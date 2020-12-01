package be.sigmadelta.common.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import be.sigmadelta.common.Preferences
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.address.AddressRepository
import be.sigmadelta.common.collections.Collection
import be.sigmadelta.common.collections.CollectionType
import be.sigmadelta.common.collections.CollectionsRepository
import be.sigmadelta.common.date.Time
import be.sigmadelta.common.db.appCtx
import be.sigmadelta.common.util.Response
import be.sigmadelta.common.util.toTime
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

private const val TAG = "NotificationRepo"

actual class NotificationRepo(
    private val context: Context,
    private val db: DB,
    private val notificationDb: DB
) {

    init {
        createNotificationChannel(appCtx)
    }

    actual fun scheduleWorker() {
        Log.e(TAG, "scheduleWorker()")
        val workBuilder = PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, workBuilder)
    }

    fun insertDefaultNotificationProps(address: Address) {
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
        Log.e(TAG, "insertDefaultNotificationProps() - notificationProps: $notificationProps")
    }

    fun getNotificationProps(address: Address) = db.find<NotificationProps>()
        .all().useModels { it.toList() }
        .firstOrNull { it.addressId == address.id }

    fun getAllNotificationProps() = db.find<NotificationProps>()
        .all().useModels { it.toList() }

    fun putTriggeredNotificationId(date: LocalDateTime, id: Int) {
        notificationDb.put(createNotificationIdLabel(date, id))
    }

    fun getTriggeredNotificationIds() = notificationDb.find<String>().all().useModels { it.toList() }

    fun updateTomorrowAlarmTime(addressId: String, alarmTime: Time) {
        val cursor = db.find<NotificationProps>().byIndex("addressId", addressId)
        if (cursor.isValid()) {
            Log.d(TAG, "updateTomorrowAlarmTime(): Cursor is valid, updating time to $alarmTime")
            val updatedModel = cursor.model().copy(genericTomorrowAlarmTime = alarmTime)
            db.deleteAll(cursor)
            db.put(updatedModel)
        } else {
            Log.e(TAG, "updateTomorrowAlarmTime(): Cursor is invalid!")
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

        override suspend fun doWork() = try {
            if (preferences.notificationsEnabled.not()) {
                Log.d(
                    "NotificationWorker",
                    "Notifications are disabled, not triggering anything"
                )
                Result.success()
            } else {

                addressRepo.getAddresses().forEach { addr ->
                    collectionRepo.searchUpcomingCollections(addr).collect { response ->
                        when (response) {
                            is Response.Success -> {
                                Log.d(
                                    TAG,
                                    "doWork() - searchUpcomingCollections(): ${response.body}"
                                )

                                Log.d(
                                    TAG,
                                    "doWork(): tomorrow = ${response.body.tomorrow?.map { it.collectionType }}"
                                )
                                response.body.tomorrow?.let {
                                    createTomorrowNotifications(
                                        it,
                                        addr
                                    )
                                }
                            }
                            is Response.Error -> Result.failure()
                            is Response.Loading -> Unit
                        }
                    }
                }
                Result.success()
            }
        } catch (e: Throwable) {
            Log.e(TAG, e.localizedMessage)
            Result.retry()
        }

        private fun createTomorrowNotifications(
            collections: List<Collection>,
            address: Address
        ) {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val now = today.toTime()
            val props = requireNotNull(notificationRepo.getNotificationProps(address))

            var text = ""
            val triggerNotification = when (collections.size) {
                0 -> false // Do nothing when empty
                1 -> {
                    text = "You have a ${collections.first().fraction.name.nl} collection tomorrow"
                    true
                }
                else -> {
                    text = "You have multiple collections tomorrow"
                    true
                }
            }

            Log.e(TAG, """
                createTomorrowNotifications(): 
                triggerNotification = $triggerNotification
                now.hasPassed(props.genericTomorrowAlarmTime) = ${now.hasPassed(props.genericTomorrowAlarmTime)}
                now = $now
                props.genericTomorrowAlarmTime = ${props.genericTomorrowAlarmTime}
            """.trimIndent())

            if (triggerNotification && now.hasPassed(props.genericTomorrowAlarmTime)) { // TODO: Test
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

                    // Every Address will only have 1 notification, this can change once multiple collection notifications are introduced
                    val notificationId = address.id.hashCode()

                    val idList = notificationRepo.getTriggeredNotificationIds()
                    if (idList.contains(createNotificationIdLabel(today, notificationId)).not()) {
                        notify(notificationId, builder.build())
                        notificationRepo.putTriggeredNotificationId(today, notificationId)
                    }
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
            .firstOrNull { it.type == collection.collectionType }
            ?.enabled == true
    }.apply {
        Log.d(TAG, "filterByEnabledNotifications(): $this")
    }

private fun createNotificationIdLabel(date: LocalDateTime, id: Int) =
    "${date.dayOfYear}${date.year}-$id"