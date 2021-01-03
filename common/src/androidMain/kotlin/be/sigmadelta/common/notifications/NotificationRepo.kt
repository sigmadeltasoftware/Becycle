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
import be.sigmadelta.Becycle.common.R
import be.sigmadelta.common.Faction
import be.sigmadelta.common.Preferences
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.address.AddressRepository
import be.sigmadelta.common.collections.*
import be.sigmadelta.common.collections.Collection
import be.sigmadelta.common.date.Time
import be.sigmadelta.common.db.appCtx
import be.sigmadelta.common.util.DBManager
import be.sigmadelta.common.util.Response
import be.sigmadelta.common.util.name
import be.sigmadelta.common.util.toTime
import com.github.aakira.napier.Napier
import kotlinx.coroutines.flow.collect
import kotlinx.datetime.*
import org.kodein.memory.util.UUID
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit


actual class NotificationRepo(
    private val context: Context,
    private val dbMan: DBManager
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

    fun insertDefaultNotificationProps(address: Address) {
        val notificationProps = NotificationProps(
            UUID.randomUUID().toString(),
            address.id,
            address.zipCode,
            address.street,
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

        dbMan.saveNotificationProps(notificationProps, address)
        Napier.d("notificationProps: $notificationProps")
    }

    fun getNotificationProps(address: Address) = dbMan.findNotificationPropsByAddress(address)

    fun getAllNotificationProps(): List<NotificationProps> {
        val propList = mutableListOf<NotificationProps>()
        Faction.values().forEach {
            propList.addAll(dbMan.findAll<NotificationProps>(it))
        }
        return propList
    }

    fun putTriggeredNotificationId(label: NotificationLabel) {
        dbMan.markNotificationTriggered(label)
    }

    fun getTriggeredNotificationIds() = dbMan.getTriggeredNotificationLabels()

    fun updateTomorrowAlarmTime(address: Address, alarmTime: Time) {
        val props = dbMan.findNotificationPropsByAddress(address)
        Napier.d("Updating time to $alarmTime")
        dbMan.saveNotificationProps(props.copy(genericTomorrowAlarmTime = alarmTime), address)
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
        private val appCtx: Context,
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
                                Napier.v("searchUpcomingCollections(): ${response.body}")

                                val tomorrowNotifications = response.body.tomorrow
                                Napier.v("tomorrow = ${tomorrowNotifications?.map { it.type }}")

                                tomorrowNotifications?.let {
                                    createTomorrowNotifications(
                                        it,
                                        addr,
                                        appCtx
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
            address: Address,
            ctx: Context
        ) {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val now = today.toTime()
            val props = requireNotNull(notificationRepo.getNotificationProps(address))

            val (text, hasCollectionTomorrow) =  when (collections.size) {
                0 -> "" to false // Do nothing when empty
                1 -> appCtx.getString(R.string.notifications_notification__text_single_collection, collections.first().type.name(ctx)) to true
                else -> appCtx.getString(R.string.notifications_notification__text_multiple_collections) to true
            }

            val notificationId = address.id.hashCode()
            val idList = notificationRepo.getTriggeredNotificationIds()
            val notificationIdLabel = createNotificationIdLabel(today, notificationId, props.genericTomorrowAlarmTime)
            val notificationWasntTriggeredBefore = idList.map{ it.id }.contains(notificationIdLabel).not()

            Napier.v("""
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
                        .setContentTitle(appCtx.getString(R.string.notifications_notification__title))
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

private fun List<Collection>.filterByEnabledNotifications(props: NotificationProps) =
    filter { collection -> // Are notifications enabled for this collection type
        props.collectionSettings
            .firstOrNull { it.type == collection.type }
            ?.enabled == true
    }.apply {
        Napier.d("filterByEnabledNotifications(): $this")
    }

private fun createNotificationIdLabel(date: LocalDateTime, id: Int, time: Time) =
    "${date.dayOfYear}${date.year}-$id-${time.hhmm}"