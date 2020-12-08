package be.sigmadelta.becycle.common.analytics

import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.ParametersBuilder
import com.google.firebase.analytics.ktx.logEvent

class AnalyticsTracker(private val firebaseAnalytics: FirebaseAnalytics) {
    fun userProp(prop: UserProps, value: String) = firebaseAnalytics.setUserProperty(prop.prop, value)
    fun log(tag: String) = firebaseAnalytics.logEvent(tag, bundleOf())
    fun log(tag: String,  block: ParametersBuilder.() -> Unit) = firebaseAnalytics.logEvent(tag, block)
}