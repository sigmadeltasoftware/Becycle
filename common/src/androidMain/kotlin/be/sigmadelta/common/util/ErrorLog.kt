package be.sigmadelta.common.util

import com.github.aakira.napier.LogLevel
import android.util.Log
import com.github.aakira.napier.Antilog

class ErrorLog: Antilog() {

    override fun performLog(priority: LogLevel, tag: String?, throwable: Throwable?, message: String?) {
        // send only error log
        if (priority < LogLevel.ERROR) return
        Log.e(tag ?: "", message ?: "")

//        throwable?.let {
//            when {
//                // e.g. http exception, add a customized your exception message
////                it is KtorException -> {
////                    Crashlytics.getInstance().core.log(priority.ordinal, "HTTP Exception", it.response?.errorBody.toString())
////                }
//            }
//        }
    }
}