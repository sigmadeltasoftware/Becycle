package be.sigmadelta.becycle.settings

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.sigmadelta.becycle.R
import be.sigmadelta.becycle.common.Destination
import be.sigmadelta.becycle.common.ui.theme.*

@Composable
fun Settings(
    goTo: (Destination) -> Unit,
    notificationSwitchState: Boolean,
    shouldShowBatteryOptimisationWarning: Boolean,
    onDisableBatteryOptimisationClicked: () -> Unit,
    onSigmaDeltaLogoClicked: () -> Unit,
    onNotificationSwitchAction: ((Boolean) -> Unit)? = null
) {
    Column(modifier = Modifier.padding(bottom = bottomNavigationMargin)) {
        SettingsMenuItem(
            title = "Address(es)",
            subtitle = "Manage your address(es) here",
            R.drawable.ic_home,
            onClickAction = { goTo(Destination.SettingsAddresses) })
        SettingsMenuItemDivider()
        SettingsMenuItem(
            title = "Notifications",
            subtitle = "Create and manage your notifications",
            R.drawable.ic_calender_clock,
            onClickAction = { goTo(Destination.SettingsNotifications) },
            notificationSwitchState,
            onNotificationSwitchAction
        )

        if (shouldShowBatteryOptimisationWarning) {
            NotificationSettingsBatteryOptimisationWarning {
                onDisableBatteryOptimisationClicked()
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Created by",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textSecondary
        )
        Image(
            asset = imageResource(id = R.drawable.ic_sigmadelta_footer),
            modifier = Modifier.padding(bottom = 8.dp, start = 96.dp, end = 96.dp)
                .clickable(onClick = {
                    onSigmaDeltaLogoClicked()
                })
        )
    }
}

@Composable
fun SettingsMenuItem(
    title: String,
    subtitle: String,
    @DrawableRes icon: Int,
    onClickAction: () -> Unit,
    switchState: Boolean? = null,
    switchAction: ((Boolean) -> Unit)? = null
) {
    val ctx = ContextAmbient.current
    Row(
        Modifier.clickable(onClick = {
            if (switchState == null || switchState) {
                onClickAction()
            } else {
                Toast.makeText(ctx, "Enable switch to modify settings", Toast.LENGTH_SHORT).show()
            }
        }, enabled = switchState == null || switchState)
            .background(color = if (switchState == null || switchState) primaryBackgroundColor else unselectedColor)
    )
    {
        Icon(
            asset = vectorResource(id = icon),
            modifier = Modifier.padding(start = 16.dp).align(Alignment.CenterVertically)
                .width(32.dp)
        )
        Column {
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, start = 16.dp)
            )
            Text(
                subtitle,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 16.dp, start = 16.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        switchAction?.let {
            Switch(
                checked = switchState == true,
                onCheckedChange = switchAction,
                color = primaryAccent,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 32.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
fun SettingsMenuItemDivider() {
    Divider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
fun NotificationSettingsBatteryOptimisationWarning(
    onDisableBatteryOptimisationClicked: () -> Unit
) {
    Column(
        modifier = Modifier.background(color = errorSecondaryColor, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                asset = vectorResource(id = R.drawable.ic_home),
                tint = errorColor,
                modifier = Modifier.padding(start = 16.dp)
            )
            Text(
                text = "Battery optimisations active, notifications might not trigger properly!",
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        Button(
            onClick = { onDisableBatteryOptimisationClicked() },
            backgroundColor = errorColor,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(text = "DISABLE BATTERY OPTIMISATIONS", color = primaryBackgroundColor)
        }
    }
}