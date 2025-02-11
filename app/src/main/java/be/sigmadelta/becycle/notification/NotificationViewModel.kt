package be.sigmadelta.becycle.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.sigmadelta.becycle.common.analytics.AnalTag
import be.sigmadelta.becycle.common.analytics.AnalyticsTracker
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.date.Time
import be.sigmadelta.common.notifications.NotifProps
import be.sigmadelta.common.notifications.NotificationRepo
import com.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val notificationRepo: NotificationRepo,
    private val analTracker: AnalyticsTracker
): ViewModel() {
    val notificationPropsViewState = MutableStateFlow<ListViewState<NotifProps>>(ListViewState.Empty())

    fun loadNotificationProps() = viewModelScope.launch {
        val notificationProps = notificationRepo.getAllNotificationProps()
        Napier.d("notificationProps = $notificationProps")
        analTracker.log(AnalTag.LOAD_NOTIFICATION_PROPS.s()){
            param("prop_count", notificationProps.size.toString())
        }
        notificationPropsViewState.value = ListViewState.Success(notificationProps)
    }

    fun setTomorrowAlarmTime(address: Address, alarmTime: Time) {
        Napier.d("addressId: ${address.id} - alarmTime: $alarmTime")
        analTracker.log(AnalTag.SET_TOMORROW_ALARM_TIME.s()) {
            param("time", alarmTime.hhmm)
        }
        notificationRepo.updateTomorrowAlarmTime(address, alarmTime)
        loadNotificationProps()
    }

    fun getTriggeredNotificationIds() = notificationRepo.getTriggeredNotificationIds()

    fun scheduleWorker() = viewModelScope.launch {
        notificationRepo.scheduleWorker()
        analTracker.log(AnalTag.SCHEDULE_WORKER.s())
    }
}