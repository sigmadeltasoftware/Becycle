package be.sigmadelta.becycle.common.analytics

import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsTracker(private val firebaseAnalytics: FirebaseAnalytics) {
    fun log(tag: String, event: Bundle?) = firebaseAnalytics.logEvent(tag, event)
    fun log(tag: String, eventName: String, eventVal: Any?) = firebaseAnalytics.logEvent(tag, bundleOf(Pair(eventName, eventVal)))
}