package be.sigmadelta.becycle.settings

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import be.sigmadelta.becycle.BuildConfig
import be.sigmadelta.becycle.R
import be.sigmadelta.becycle.common.Destination
import be.sigmadelta.becycle.common.ui.theme.*
import be.sigmadelta.becycle.common.util.str

@Composable
fun Settings(
    goTo: (Destination) -> Unit,
    notificationSwitchState: Boolean,
    shouldShowBatteryOptimisationWarning: Boolean,
    onDisableBatteryOptimisationClicked: () -> Unit,
    onGetDisableBatteryOptimisationInfoClicked: () -> Unit,
    shouldShowAutoStarterWarning: Boolean,
    onAutoStarterClicked: () -> Unit,
    onSigmaDeltaLogoClicked: () -> Unit,
    onSendFeedbackClicked: () -> Unit,
    onNotificationSwitchAction: ((Boolean) -> Unit)? = null,
) {
    Column(modifier = Modifier.padding(bottom = bottomNavigationMargin)) {
        SettingsMenuItem(
            title = R.string.settings__addresses.str(),
            subtitle = R.string.settings__addresses_text.str(),
            R.drawable.ic_home,
            onClickAction = { goTo(Destination.SettingsAddresses) }
        )
        SettingsMenuItemDivider()
        SettingsMenuItem(
            title = R.string.notifications.str(),
            subtitle = R.string.settings__notifications_text.str(),
            R.drawable.ic_calender_clock,
            onClickAction = { goTo(Destination.SettingsNotifications) },
            notificationSwitchState,
            onNotificationSwitchAction
        )

        if (shouldShowBatteryOptimisationWarning) {
            NotificationSettingsBatteryOptimisationWarning(
                onDisableBatteryOptimisationClicked = { onDisableBatteryOptimisationClicked() },
                onGetDisableBatteryOptimisationInfoClicked = { onGetDisableBatteryOptimisationInfoClicked() }
            )
        }
        SettingsMenuItemDivider()
        SettingsMenuItem(
            title = R.string.send_feedback.str(),
            subtitle = R.string.settings__send_feedback_text.str(),
            icon = R.drawable.ic_mail,
            onClickAction = { onSendFeedbackClicked() }
        )

        Spacer(modifier = Modifier.weight(1f))

        SettingsFooter(onSigmaDeltaLogoClicked)
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
    val ctx = AmbientContext.current
    Row(
        Modifier.clickable(onClick = {
            if (switchState == null || switchState) {
                onClickAction()
            } else {
                Toast.makeText(
                    ctx,
                    R.string.settings_switch__modify_settings,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, enabled = switchState == null || switchState)
            .background(color = if (switchState == null || switchState) primaryBackgroundColor else unselectedColor)
    )
    {
        Icon(
            imageVector = vectorResource(id = icon),
            modifier = Modifier.padding(start = 16.dp).align(Alignment.CenterVertically)
                .width(32.dp)
        )
        Column {
            Text(
                title,
                fontSize = regularFontSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, start = 16.dp)
            )
            Text(
                subtitle,
                fontSize = subTextFontSize,
                modifier = Modifier.padding(bottom = 16.dp, start = 16.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        switchAction?.let {
            Switch(
                checked = switchState == true,
                onCheckedChange = switchAction,
                colors = SwitchDefaults.colors(checkedThumbColor = primaryAccent),
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
    onDisableBatteryOptimisationClicked: () -> Unit,
    onGetDisableBatteryOptimisationInfoClicked: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.background(
                color = errorSecondaryColor,
                shape = RoundedCornerShape(12.dp)
            ).fillMaxWidth().padding(vertical = 16.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = vectorResource(id = R.drawable.ic_info),
                tint = warningColor,
                modifier = Modifier.clickable(onClick = {
                    onGetDisableBatteryOptimisationInfoClicked()
                })
            )
            Button(
                onClick = { onDisableBatteryOptimisationClicked() },
                colors = ButtonDefaults.buttonColors(backgroundColor = errorColor),
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = R.string.settings__disable_battery_optimisations.str(),
                    color = primaryBackgroundColor
                )
            }
        }
    }
}

@Composable
fun NotificationSettingsAutoStarterWarning(
    onAutoStarterClicked: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.background(
                color = errorSecondaryColor,
                shape = RoundedCornerShape(12.dp)
            ).fillMaxWidth().padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = vectorResource(id = R.drawable.ic_info),
                tint = warningColor,
                modifier = Modifier.padding(start = 8.dp).clickable(onClick = {

                })
            )
            Button(
                onClick = { onAutoStarterClicked() },
                colors = ButtonDefaults.buttonColors(backgroundColor = errorColor),
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = R.string.settings__give_higher_prio.str(),
                    color = primaryBackgroundColor
                )
            }
        }
    }
}

@Composable
fun SettingsFooter(
    onSigmaDeltaLogoClicked: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Text(
            text = "${R.string.version.str()}: V${BuildConfig.VERSION_NAME}",
            fontSize = minimalFontSize,
            fontWeight = FontWeight.Bold,
            color = textPrimary
        )
        Text(
            text = R.string.created_by.str(),
            fontSize = minimalFontSize,
            fontWeight = FontWeight.Bold,
            color = textSecondary
        )
        Image(imageResource(id = R.drawable.ic_sigmadelta_footer),
            modifier = Modifier.padding(bottom = 8.dp, start = 128.dp, end = 128.dp)
                .clickable(onClick = {
                    onSigmaDeltaLogoClicked()
                })
        )
    }
}