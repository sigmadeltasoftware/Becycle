package be.sigmadelta.becycle.notification

import android.app.TimePickerDialog
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.work.WorkManager
import be.sigmadelta.becycle.BuildConfig
import be.sigmadelta.becycle.R
import be.sigmadelta.becycle.address.AddressSwitcher
import be.sigmadelta.becycle.common.ui.theme.*
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.date.Time
import be.sigmadelta.common.notifications.NotificationProps
import be.sigmadelta.common.notifications.NotificationRepo

@Composable
fun SettingsNotifications(
    addresses: ListViewState<Address>,
    notificationProps: ListViewState<NotificationProps>,
    onGoToAddressInput: () -> Unit,
    onTomorrowAlarmTimeSelected: (addressId: String, alarmTime: Time) -> Unit,
    onNotificationsInfoClicked: () -> Unit,
    onReloadNotificationPropsWhenEmpty: () -> Unit
) {
    var selectedTabIx by remember { mutableStateOf(0) }

    Column {
        AddressSwitcher(selectedTabIx, addresses, onGoToAddressInput) { ix -> selectedTabIx = ix }

        Row(
            modifier = Modifier.padding(horizontal = 16.dp).padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Schedule notifications",
                fontSize = titleFontSize,
                color = textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { onNotificationsInfoClicked() }) {
                Icon(asset = vectorResource(id = R.drawable.ic_info), tint = secondaryAccent)
            }
        }

        Crossfade(selectedTabIx) { newIx ->
            (addresses as? ListViewState.Success)?.payload?.let { addresses ->
                if (addresses.size > newIx) {
                    (notificationProps as? ListViewState.Success)?.payload?.let { props ->
                        props.firstOrNull { it.addressId == addresses[newIx].id }?.let {
                            NotificationSettings(it) { alarmTime ->
                                onTomorrowAlarmTimeSelected(addresses[newIx].id, alarmTime)
                            }
                        }

                        if (props.isNullOrEmpty()) {
                            onReloadNotificationPropsWhenEmpty()
                        }
                    }
                }
            }
        }

        if (BuildConfig.DEBUG) {
            val workInfos = WorkManager.getInstance(ContextAmbient.current)
                .getWorkInfosForUniqueWork(NotificationRepo.WORK_NAME)
                .get()
            if (workInfos.size == 1) {
                Text(text = "NotifWorker state: ${workInfos[0].state}")
                Text(text = "NotifWorker id(s): ${workInfos[0].id}")
            }
        }
    }
}

@Composable
fun NotificationSettings(
    notificationProps: NotificationProps,
    onTomorrowAlarmTimeSelected: (Time) -> Unit
) {
    var notificationTimeToday by remember { mutableStateOf(notificationProps.genericTodayAlarmTime) }
    var notificationTimeTomorrow by remember { mutableStateOf(notificationProps.genericTomorrowAlarmTime) }
    val ctx = ContextAmbient.current

    Row(modifier = Modifier.padding(16.dp)) {
        Column(modifier = Modifier.align(alignment = Alignment.CenterVertically)) {
            Text(
                text = "Tomorrow:",
                fontSize = regularFontSize,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )
            Text(
                text = "Notification for tomorrow's collection",
                fontSize = subTextFontSize,
                color = textSecondary,
                modifier = Modifier.width(240.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = {
            TimePickerDialog(
                ctx,
                R.style.timePickerTheme,
                { _, hr, min ->
                    notificationTimeTomorrow = Time(hr, min)
                    onTomorrowAlarmTimeSelected(notificationTimeTomorrow)
                },
                notificationTimeTomorrow.hours,
                notificationTimeTomorrow.mins,
                true
            ).show()
        }, modifier = Modifier.align(alignment = Alignment.CenterVertically)) {
            Row {
                Icon(vectorResource(id = R.drawable.ic_notification))
                Text(text = notificationTimeTomorrow.hhmm, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

