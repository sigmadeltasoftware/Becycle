package be.sigmadelta.common.util

import be.sigmadelta.Becycle.common.BuildConfig
import com.github.aakira.napier.DebugAntilog
import com.github.aakira.napier.Napier

actual fun initLogger() {
    if (BuildConfig.DEBUG) {
        Napier.base(DebugAntilog())
    } else {
        Napier.base(ErrorLog())
    }
}