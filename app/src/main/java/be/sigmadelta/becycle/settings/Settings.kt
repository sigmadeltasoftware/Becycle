package be.sigmadelta.becycle.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.sigmadelta.becycle.R
import be.sigmadelta.becycle.common.Destination

@Composable
fun Settings(
    goTo: (Destination) -> Unit,
    notificationSwitchState: State<Boolean>,
    onNotificationSwitchAction: ((Boolean) -> Unit)? = null
) {
    Column {
        SettingsMenuItem(title = "Address(es)", subtitle = "Manage your address(es) here", R.drawable.ic_home, onClickAction = { goTo(Destination.SettingsAddresses) })
        SettingsMenuItemDivider()
        SettingsMenuItem(title = "Notifications", subtitle = "Create and manage your notifications", R.drawable.ic_calender_clock, onClickAction = { goTo(Destination.SettingsNotifications) }, notificationSwitchState, onNotificationSwitchAction)
    }
}

@Composable
fun SettingsMenuItem(
    title: String,
    subtitle: String,
    @DrawableRes icon: Int,
    onClickAction: () -> Unit,
    switchState: State<Boolean>? = null,
    switchAction: ((Boolean) -> Unit)? = null
) {
    Row(Modifier.clickable(onClick = {
        if (switchState?.value == true) onClickAction()
    })) {
        Icon(asset = vectorResource(id = icon), modifier = Modifier.padding(start = 16.dp).align(Alignment.CenterVertically).width(32.dp))
        Column(modifier = Modifier) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, start = 16.dp))
            Text(subtitle, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp, start = 16.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        switchAction?.let {
            Switch(checked = switchState?.value == true, onCheckedChange = switchAction, modifier = Modifier.padding(horizontal = 16.dp, vertical = 32.dp).align(Alignment.CenterVertically))
        }
    }
}

@Composable
fun SettingsMenuItemDivider() {
    Divider(modifier = Modifier.padding(horizontal = 16.dp))
}