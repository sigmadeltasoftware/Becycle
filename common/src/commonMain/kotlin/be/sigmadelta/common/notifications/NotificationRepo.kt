package be.sigmadelta.common.notifications

expect class NotificationRepo {
    suspend fun scheduleWorker()
}