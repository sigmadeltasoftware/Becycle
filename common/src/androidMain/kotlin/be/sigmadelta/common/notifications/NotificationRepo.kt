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
import be.sigmadelta.common.db.appCtx
import be.sigmadelta.common.util.Response
import kotlinx.coroutines.coroutineScope
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
import kotlin.random.Random

private const val TAG = "NotificationRepo"

actual class NotificationRepo(
    private val context: Context,
    private val db: DB
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
                CollectionNotificationProps(it, true)
            }
        )

        val findPrevious = db.find<NotificationProps>().byIndex("addressId", address.id)
        db.deleteAll(findPrevious)
        db.put(notificationProps)
        Log.e(TAG, "insertDefaultNotificationProps() - notificationProps: $notificationProps")
    }

    fun getNotificationProps(address: Address) = db.find<NotificationProps>()
        .all().useModels { it.toList() }
        .firstOrNull { it.addressId == address.id }

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
                    val props = notificationRepo.getNotificationProps(addr)
                    collectionRepo.searchUpcomingCollections(addr).collect { response ->
                        when (response) {
                            is Response.Success -> {
                                val now = Clock.System.now()
                                    .toLocalDateTime(TimeZone.currentSystemDefault())

                                Log.d(TAG, "searchUpcomingCollections(): ${response.body}")

                                response.body.tomorrow?.filterByEnabledNotifications(requireNotNull(props))
                                    ?.forEach {

                                        // if (now is later than notification time) {
                                        val builder =
                                            NotificationCompat.Builder(appCtx, notif_chan_id)
                                                .setContentTitle("You have a collection tomorrow for ${it.fraction.logo.toCollectionType().name}")
                                                .setSmallIcon(R.drawable.notification_icon_background)
                                                .setPriority(NotificationCompat.PRIORITY_HIGH)

                                        with(NotificationManagerCompat.from(appCtx)) {
                                            // notificationId is a unique int for each notification that you must define
                                            notify(Random(2).nextInt(), builder.build())
                                        }
                                        // }
                                    }
                            }
                            is Response.Error -> Result.failure()
                        }
                    }
                }
                Result.success()
            }
        } catch (e: Throwable) {
            Log.e(TAG, e.localizedMessage)
            Result.retry()
        }
    }

    companion object {
        private const val notif_chan_id = "BECYCLE_NOTIFICATIONS"
        private const val notif_chan_name = "Becycle Notifications"
        private const val notif_chan_desc = "Reminder notifications for Becycle"
        const val WORK_NAME = "BECYCLE_NOTIF_WORK"
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