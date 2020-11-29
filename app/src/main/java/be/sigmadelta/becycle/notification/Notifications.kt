package be.sigmadelta.becycle.notification

import android.app.TimePickerDialog
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import be.sigmadelta.becycle.R
import be.sigmadelta.becycle.address.AddressSwitcher
import be.sigmadelta.becycle.common.ui.theme.*
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.notifications.NotificationProps
import be.sigmadelta.common.util.addLeadingZeroBelow10

@Composable
fun SettingsNotifications(
    addresses: ListViewState<Address>,
    notificationProps: ListViewState<NotificationProps>,
    onGoToAddressInput: () -> Unit,
    onTomorrowAlarmTimeSelected: (addressId: String, alarmTime: String) -> Unit
) {
    var selectedTabIx by remember { mutableStateOf(0) }

    Column {
        AddressSwitcher(selectedTabIx, addresses, onGoToAddressInput) { ix -> selectedTabIx = ix }

        Text(
            text = "Schedule notifications",
            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
            fontSize = titleFontSize,
            color = textPrimary,
            fontWeight = FontWeight.Bold
        )
        Crossfade(selectedTabIx) { newIx ->
            (addresses as? ListViewState.Success)?.payload?.let { addresses ->
                if (addresses.size > newIx) {
                    (notificationProps as? ListViewState.Success)?.payload?.let { props ->
                        props.firstOrNull { it.addressId == addresses[newIx].id }?.let {
                            NotificationSettings(it) { alarmTime ->
                                onTomorrowAlarmTimeSelected(addresses[newIx].id, alarmTime)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationSettings(
    notificationProps: NotificationProps,
    onTomorrowAlarmTimeSelected: (String) -> Unit
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
                    notificationTimeTomorrow = "${addLeadingZeroBelow10(hr)}:${addLeadingZeroBelow10(min)}"
                    onTomorrowAlarmTimeSelected(notificationTimeTomorrow)
                },
                notificationTimeTomorrow.substringBefore(":").toInt(),
                notificationTimeTomorrow.substringAfter(":").toInt(),
                true
            ).show()
        }, modifier = Modifier.align(alignment = Alignment.CenterVertically)) {
            Row {
                Icon(vectorResource(id = R.drawable.ic_notification))
                Text(text = notificationTimeTomorrow, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

