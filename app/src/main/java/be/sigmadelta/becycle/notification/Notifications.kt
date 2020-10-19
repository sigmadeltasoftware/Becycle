package be.sigmadelta.becycle.notification

import android.app.TimePickerDialog
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.WorkManager
import be.sigmadelta.becycle.R
import be.sigmadelta.becycle.common.Destination
import be.sigmadelta.becycle.common.ui.theme.primaryAccent
import be.sigmadelta.becycle.common.ui.theme.primaryBackgroundColor
import be.sigmadelta.becycle.common.ui.theme.secondaryAccent
import be.sigmadelta.becycle.common.ui.theme.unselectedColor
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.notifications.NotificationRepo
import be.sigmadelta.common.util.addLeadingZeroBelow10

@Composable
fun SettingsNotifications(
    addresses: ListViewState<Address>,
    onGoToAddressInput: () -> Unit
) {
    var selectedTabIx by remember { mutableStateOf(0) }

    Column {
        AddressSwitcher(selectedTabIx, addresses, onGoToAddressInput) { ix -> selectedTabIx = ix }

        Crossfade(selectedTabIx) { newIx ->
            (addresses as? ListViewState.Success)?.let {
                if (it.payload.size > newIx) {
                    NotificationSettings(address = it.payload[newIx])
                }
            }
        }

        val notifWorker = WorkManager.getInstance(ContextAmbient.current)
            .getWorkInfosByTag(NotificationRepo.WORK_NAME)
        Text(text = "NotifWorker is cancelled: ${notifWorker.isCancelled}")
        Text(text = "NotifWorker is done: ${notifWorker.isDone}")
    }
}

@Composable
fun AddressSwitcher(
    selectedTabIx: Int,
    addresses: ListViewState<Address>,
    onGoToAddressInput: () -> Unit,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTabIx,
        backgroundColor = primaryBackgroundColor,
        contentColor = primaryAccent,
        divider = { Divider() }
    ) {
        when (addresses) {
            is ListViewState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            is ListViewState.Success -> {
                addresses.payload.forEachIndexed { ix, it ->
                    Tab(
                        selected = selectedTabIx == ix,
                        onClick = { onTabSelected(ix) },
                        modifier = Modifier
                            .background(if (selectedTabIx == ix) secondaryAccent else primaryBackgroundColor)
                            .padding(16.dp),
                        selectedContentColor = primaryAccent,
                        unselectedContentColor = primaryBackgroundColor,
                    ) {
                        Text(
                            it.zipCodeItem.code,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIx == ix) primaryAccent else unselectedColor
                        )
                        Text(
                            "${it.street.names.nl} ${it.houseNumber}",
                            fontSize = 10.sp,
                            color = if (selectedTabIx == ix) primaryAccent else unselectedColor
                        )
                    }
                }

                if (addresses.payload.size < 5) {
                    Tab(selected = false, onClick = onGoToAddressInput) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = "Add Address",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = unselectedColor
                            )
                            Icon(
                                asset = vectorResource(id = R.drawable.ic_add),
                                tint = primaryAccent,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
            is ListViewState.Error -> {
                Log.e("AddressSwitcher", addresses.error?.localizedMessage.toString())
                Text("Unable to retrieve address data", color = Color.Red)
            }
        }
    }
}

@Composable
fun NotificationSettings(address: Address) {
    var notificationTime by remember { mutableStateOf("07:00") }
    val ctx = ContextAmbient.current

    Text("Enable notifications for")
    Row(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "A day up front:",
            modifier = Modifier.align(alignment = Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = {
            TimePickerDialog(
                ctx,
                R.style.timePickerTheme,
                { _, hr, min ->
                    notificationTime = "${addLeadingZeroBelow10(hr)}:${addLeadingZeroBelow10(min)}"
                },
                notificationTime.substringBefore(":").toInt(),
                notificationTime.substringAfter(":").toInt(),
                true
            ).show()
        }, modifier = Modifier.padding(16.dp).align(alignment = Alignment.CenterVertically)) {
            Row {
                Icon(vectorResource(id = R.drawable.ic_notification))
                Text(text = notificationTime, color = Color.White)
            }
        }
    }
}

