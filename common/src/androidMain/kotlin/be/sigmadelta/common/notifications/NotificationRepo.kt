package be.sigmadelta.common.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import be.sigmadelta.common.Preferences
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.address.RecAppAddressDao
import be.sigmadelta.common.address.AddressRepository
import be.sigmadelta.common.collections.Collection
import be.sigmadelta.common.collections.recapp.RecAppCollectionDao
import be.sigmadelta.common.collections.CollectionType
import be.sigmadelta.common.collections.CollectionsRepository
import be.sigmadelta.common.date.Time
import be.sigmadelta.common.db.appCtx
import be.sigmadelta.common.util.Response
import be.sigmadelta.common.util.toTime
import com.github.aakira.napier.Napier
import kotlinx.coroutines.flow.collect
import kotlinx.datetime.*
import org.kodein.db.DB
import org.kodein.db.deleteAll
import org.kodein.db.find
import org.kodein.db.useModels
import org.kodein.memory.util.UUID
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit


actual class NotificationRepo(
    private val context: Context,
    private val db: DB,
    private val notificationDb: DB
) {

    init {
        createNotificationChannel(appCtx)
    }

    actual suspend fun scheduleWorker() {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(WORK_NAME).await().let {
            Napier.d("cancelAllWorkByTag result = $it")
        }
        workManager.pruneWork().await().let {
            Napier.d("pruneWork result = $it")
        }
        val workBuilder = PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES)
            .build()
        workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, workBuilder)
    }

    fun insertDefaultNotificationProps(address: RecAppAddressDao) {
        val notificationProps = NotificationProps(
            UUID.randomUUID().toString(),
            address.id,
            address.zipCodeItem.code,
            address.street.names,
            CollectionType.values().map {
                CollectionNotificationProps(
                    it,
                    true,
                    Time.parseHhMm(DEFAULT_TODAY_TIME)!!,
                    Time.parseHhMm(DEFAULT_TOMORROW_TIME)!!
                )
            },
            Time.parseHhMm(DEFAULT_TODAY_TIME)!!,
            Time.parseHhMm(DEFAULT_TOMORROW_TIME)!!
        )

        val findPrevious = db.find<NotificationProps>().byIndex("addressId", address.id)
        db.deleteAll(findPrevious)
        db.put(notificationProps)
        Napier.d("notificationProps: $notificationProps")
    }

    fun getNotificationProps(address: Address) = db.find<NotificationProps>()
        .all().useModels { it.toList() }
        .firstOrNull { it.addressId == address.id }

    fun getAllNotificationProps() = db.find<NotificationProps>()
        .all().useModels { it.toList() }

    fun putTriggeredNotificationId(label: NotificationLabel) {
        notificationDb.put(label)
    }

    fun getTriggeredNotificationIds() = notificationDb.find<NotificationLabel>().all().useModels { it.toList() }

    fun updateTomorrowAlarmTime(addressId: String, alarmTime: Time) {
        val cursor = db.find<NotificationProps>().byIndex("addressId", addressId)
        if (cursor.isValid()) {
            Napier.d("Cursor is valid, updating time to $alarmTime")
            val updatedModel = cursor.model().copy(genericTomorrowAlarmTime = alarmTime)
            db.deleteAll(cursor)
            db.put(updatedModel)
        } else {
            Napier.e("Cursor is invalid!")
        }
    }

    @SuppressLint("ServiceCast")
    private fun createNotificationChannel(appCtx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(notif_chan_id, notif_chan_name, importance).apply {
                description = notif_chan_desc
            }

            (appCtx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    class NotificationWorker(
        appCtx: Context,
        workerParams: WorkerParameters
    ) : CoroutineWorker(appCtx, workerParams), KoinComponent {

        private val preferences: Preferences by inject()
        private val addressRepo: AddressRepository by inject()
        private val collectionRepo: CollectionsRepository by inject()
        private val notificationRepo: NotificationRepo by inject()
        private val notificationIntent: PendingIntent by inject()

        override suspend fun doWork() = try {
            if (preferences.notificationsEnabled.not()) {
                Napier.d("Notifications are disabled, not triggering anything")
                Result.success()
            } else {

                addressRepo.getAddresses().forEach { addr ->
                    collectionRepo.searchUpcomingCollections(addr, shouldNotFetch = true).collect { response ->
                        when (response) {
                            is Response.Success -> {
                                Napier.d("searchUpcomingCollections(): ${response.body}")

                                val tomorrowNotifications = response.body.tomorrow
                                Napier.d("tomorrow = ${tomorrowNotifications?.map { it.type }}")

                                tomorrowNotifications?.let {
                                    createTomorrowNotifications(
                                        it,
                                        addr
                                    )
                                }
                            }
                            is Response.Error -> Napier.d(response.error?.localizedMessage ?: "Error occurred during searchUpcomingCollections")
                            is Response.Loading -> Unit
                        }
                    }
                }
                Result.success()
            }
        } catch (e: Throwable) {
            Napier.e(e.localizedMessage)
            Result.retry()
        }

        private fun createTomorrowNotifications(
            collections: List<Collection>,
            address: Address
        ) {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val now = today.toTime()
            val props = requireNotNull(notificationRepo.getNotificationProps(address))

            val (text, hasCollectionTomorrow) =  when (collections.size) {
                0 -> "" to false // Do nothing when empty
                // TODO: Look into better way to extract names (maybe through strings?)
                1 -> "You have a ${collections.first().type.name} collection tomorrow" to true
                else -> "You have multiple collections tomorrow" to true
            }

            val notificationId = address.id.hashCode()
            val idList = notificationRepo.getTriggeredNotificationIds()
            val notificationIdLabel = createNotificationIdLabel(today, notificationId, props.genericTomorrowAlarmTime)
            val notificationWasntTriggeredBefore = idList.map{ it.id }.contains(notificationIdLabel).not()

            Napier.d("""
                createTomorrowNotifications(): 
                hasCollectionTomorrow = $hasCollectionTomorrow
                now.hasPassed(props.genericTomorrowAlarmTime) = ${now.hasPassed(props.genericTomorrowAlarmTime)}
                now = $now
                props.genericTomorrowAlarmTime = ${props.genericTomorrowAlarmTime}
                notificationIdLabel = $notificationIdLabel
                idList = $idList
                notificationWasntTriggeredBefore = $notificationWasntTriggeredBefore
            """.trimIndent())

            if (
                hasCollectionTomorrow &&
                notificationWasntTriggeredBefore &&
                now.hasPassed(props.genericTomorrowAlarmTime)
            ) {
                notificationRepo.putTriggeredNotificationId(NotificationLabel(notificationIdLabel))

                with(NotificationManagerCompat.from(appCtx)) {
                    val builder = NotificationCompat.Builder(appCtx, notif_chan_id)
                        .setContentTitle("Collection Tomorrow!")
                        .setSmallIcon(preferences.androidNotificationIconRef)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setStyle(
                            NotificationCompat.BigTextStyle()
                                .setBigContentTitle("Collection for ${address.fullAddress}")
                                .bigText(text)
                        )
                        .setAutoCancel(true)
                        .setContentIntent(notificationIntent)

                    // Every Address will only have 1 notification, this can change once multiple collection notifications are introduced

                    notify(notificationId, builder.build())
                }
            }
        }
    }

    companion object {
        private const val notif_chan_id = "BECYCLE_NOTIFICATIONS"
        private const val notif_chan_name = "Becycle Notifications"
        private const val notif_chan_desc = "Reminder notifications for Becycle"
        const val WORK_NAME = "BECYCLE_NOTIF_WORK"
        private const val DEFAULT_TODAY_TIME = "06:00"
        private const val DEFAULT_TOMORROW_TIME = "18:00"
    }
}

private fun List<RecAppCollectionDao>.filterByEnabledNotifications(props: NotificationProps) =
    filter { collection -> // Are notifications enabled for this collection type
        props.collectionSettings
            .firstOrNull { it.type == collection.collectionType }
            ?.enabled == true
    }.apply {
        Napier.d("filterByEnabledNotifications(): $this")
    }

private fun createNotificationIdLabel(date: LocalDateTime, id: Int, time: Time) =
    "${date.dayOfYear}${date.year}-$id-${time.hhmm}"