package be.sigmadelta.common

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.invoke

class Preferences {
    companion object {
        const val NOTIFS_ENABLED = "NOTIFS_ENABLED"
        const val IS_FIRST_RUN = "IS_FIRST_RUN"
        const val NOTIFS_ICON_REF = "NOTIFS_ICON_REF"
        const val HAS_MIGRATED = "HAS_MIGRATED"
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

    var hasMigrated: Boolean
        get() = settings[HAS_MIGRATED] ?: false
        set(value) = settings.putBoolean(HAS_MIGRATED, value)

    // - Make notification persistent
    // - Snooze notification from within drawer itself (5min - 1h)
    // - Same day/day before
}