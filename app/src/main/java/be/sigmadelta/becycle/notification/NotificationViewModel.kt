package be.sigmadelta.becycle.notification

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.sigmadelta.becycle.common.analytics.AnalyticsTracker
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.common.notifications.NotificationProps
import be.sigmadelta.common.notifications.NotificationRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val notificationRepo: NotificationRepo,
    private val analTracker: AnalyticsTracker
): ViewModel() {
    val notificationPropsViewState = MutableStateFlow<ListViewState<NotificationProps>>(ListViewState.Empty())

    fun loadNotificationProps() = viewModelScope.launch {
        val notificationProps = notificationRepo.getAllNotificationProps()
        Log.d(TAG, "loadNotificationProps(): $notificationProps")
        analTracker.log(ANAL_TAG, "loadNotificationProps", "Notification Props count: ${notificationProps.size}")
        notificationPropsViewState.value = ListViewState.Success(notificationProps)
    }

    fun setTomorrowAlarmTime(addressId: String, alarmTime: String) {
        Log.d(TAG, "updateTomorrowAlarmTime(): addressId: $addressId - alarmTime: $alarmTime")
        notificationRepo.updateTomorrowAlarmTime(addressId, alarmTime)
        loadNotificationProps()
    }

    companion object {
        private const val TAG = "NotificationViewModel"
        private const val ANAL_TAG = "NotificationVM"
    }
}