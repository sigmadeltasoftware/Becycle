package be.sigmadelta.becycle.notification

import android.app.TimePickerDialog
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
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
import be.sigmadelta.becycle.R
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.util.addLeadingZeroBelow10

@Composable
fun Notifications(addresses: ListViewState<Address>) {
    var selectedTabIx by remember { mutableStateOf(0) }

    Column {
        TabRow(selectedTabIndex = selectedTabIx, divider = { Divider() }) {
            when (addresses) {
                is ListViewState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                is ListViewState.Success -> addresses.payload.forEachIndexed { ix, it ->
                    Tab(selected = selectedTabIx == ix, onClick = {
                        selectedTabIx = ix
                    }, modifier = Modifier.padding(16.dp)) {
                        Text(it.zipCodeItem.code, fontWeight = FontWeight.Bold)
                        Text(it.street.names.nl, fontSize = 10.sp)
                    }
                }
                is ListViewState.Error -> Text("Unable to retrieve address data", color = Color.Red)
            }
        }

        Crossfade(selectedTabIx) { newIx ->
            (addresses as? ListViewState.Success)?.let {
                if (it.payload.size > newIx) {
                    NotificationSettings(address = it.payload[newIx])
                }
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
        Text(text = "A day up front:", modifier = Modifier.align(alignment = Alignment.CenterVertically))
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = {
            TimePickerDialog(
                ctx,
                { _, hr, min ->
                    notificationTime = "${addLeadingZeroBelow10(hr)}:${addLeadingZeroBelow10(min)}"
                 },
                notificationTime.substringBefore(":").toInt(),
                notificationTime.substringAfter(":").toInt() ,
                true).show()
        }, modifier = Modifier.padding(16.dp).align(alignment = Alignment.CenterVertically)) {
            Row {
                Icon(vectorResource(id = R.drawable.ic_notification))
                Text(text = notificationTime, color = Color.White)
            }
        }
    }
}

