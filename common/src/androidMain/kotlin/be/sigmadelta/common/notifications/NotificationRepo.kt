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
import be.sigmadelta.common.db.appCtx
import java.util.concurrent.TimeUnit
import kotlin.random.Random


actual class NotificationRepo(private val context: Context) {

    init {
        createNotificationChannel(appCtx)
    }

    actual fun scheduleWorker() {
        Log.e("NotificationRepo", "triggerNotification()")
        val workBuilder = PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, workBuilder)
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
    ) : Worker(appCtx, workerParams) {

        // We cannot rely on the validity of KOIN in this situation, need to instantiate our dependencies manually
        private val preferences = Preferences()

        override fun doWork() = try {
            if (preferences.notificationsEnabled.not()) {
                Log.d("NotificationWorker", "Notifications are disabled, not triggering anything")
                Result.success()
            } else {
                val builder = NotificationCompat.Builder(appCtx, notif_chan_id)
                    .setContentTitle("Kut, notifs are " + if (preferences.notificationsEnabled) "enabled" else "disabled")
                    .setSmallIcon(R.drawable.notification_icon_background)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)

                with(NotificationManagerCompat.from(appCtx)) {
                    // notificationId is a unique int for each notification that you must define
                    notify(Random(2).nextInt(), builder.build())
                }

                Result.success()
            }
        } catch (e: Throwable) {
            Result.retry()
        }
    }

    companion object {
        private const val notif_chan_id = "BECYCLE_NOTIFICATIONS"
        private const val notif_chan_name = "Becycle Notifications"
        private const val notif_chan_desc = "Reminder notifications for Becycle"
        private const val WORK_NAME = "BECYCLE_NOTIF_WORK"
    }
}