package be.sigmadelta.common

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.invoke

class Preferences {
    companion object {
        const val NOTIFS_ENABLED = "NOTIFS_ENABLED"
        val settings = Settings.Companion.invoke()
    }

    var notificationsEnabled: Boolean
        get() = settings[NOTIFS_ENABLED] ?: true
        set(value) = settings.putBoolean(NOTIFS_ENABLED, value)

    // - Make notification persistent
    // - Snooze notification from within drawer itself (5min - 1h)
    // - Same day/day before
    fun get() {

    }
}