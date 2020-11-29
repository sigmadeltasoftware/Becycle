package be.sigmadelta.common

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.invoke

class Preferences {
    companion object {
        const val NOTIFS_ENABLED = "NOTIFS_ENABLED"
        const val IS_FIRST_RUN = "IS_FIRST_RUN"
        const val NOTIFS_ICON_REF = "NOTIFS_ICON_REF"
        val settings = Settings.Companion.invoke()
    }

    var notificationsEnabled: Boolean
        get() = settings[NOTIFS_ENABLED] ?: true
        set(value) = settings.putBoolean(NOTIFS_ENABLED, value)

    var androidNotificationIconRef: Int
        get() = settings[NOTIFS_ICON_REF] ?: 0
        set(value) = settings.putInt(NOTIFS_ICON_REF, value)

    var isFirstRun: Boolean
        get() = settings[IS_FIRST_RUN] ?: true
        set(value) = settings.putBoolean(IS_FIRST_RUN, value)

    // - Make notification persistent
    // - Snooze notification from within drawer itself (5min - 1h)
    // - Same day/day before
}