package be.sigmadelta.becycle.common.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat

class PowerUtil {
    companion object {
        fun isIgnoringBatteryOptimizations(context: Context): Boolean {
            val powerMan =
                context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
            val name = context.packageName
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return powerMan.isIgnoringBatteryOptimizations(name)
            }
            return true
        }

        fun checkBattery(context: Context) {
            if (!isIgnoringBatteryOptimizations(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:${context.packageName}")
                    ContextCompat.startActivity(context, intent, null)
                } catch (e: ActivityNotFoundException) {
                    Log.e(
                        "PowerUtil",
                        e.localizedMessage ?: "Activity not found for battery optimization"
                    )
                    e.printStackTrace()
                }
            }
        }
    }
}