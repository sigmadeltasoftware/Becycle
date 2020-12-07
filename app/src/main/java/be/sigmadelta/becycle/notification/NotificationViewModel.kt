package be.sigmadelta.becycle.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.sigmadelta.becycle.common.analytics.AnalyticsTracker
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.common.date.Time
import be.sigmadelta.common.notifications.NotificationProps
import be.sigmadelta.common.notifications.NotificationRepo
import com.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val notificationRepo: NotificationRepo,
    private val analTracker: AnalyticsTracker
): ViewModel() {
    val notificationPropsViewState = MutableStateFlow<ListViewState<NotificationProps>>(ListViewState.Empty())

    fun loadNotificationProps() = viewModelScope.launch {
        val notificationProps = notificationRepo.getAllNotificationProps()
        Napier.d("notificationProps = $notificationProps")
        analTracker.log(ANAL_TAG, "loadNotificationProps", "Notification Props count: ${notificationProps.size}")
        notificationPropsViewState.value = ListViewState.Success(notificationProps)
    }

    fun setTomorrowAlarmTime(addressId: String, alarmTime: Time) {
        Napier.d("addressId: $addressId - alarmTime: $alarmTime")
        notificationRepo.updateTomorrowAlarmTime(addressId, alarmTime)
        loadNotificationProps()
    }

    fun getTriggeredNotificationIds() = notificationRepo.getTriggeredNotificationIds()

    fun scheduleWorker() = viewModelScope.launch {
        notificationRepo.scheduleWorker()
    }

    companion object {
        private const val ANAL_TAG = "NotificationVM"
    }
}