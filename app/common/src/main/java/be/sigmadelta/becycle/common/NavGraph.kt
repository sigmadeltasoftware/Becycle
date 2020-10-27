package be.sigmadelta.becycle.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class Destination: Parcelable {

    @Parcelize
    object Home: Destination()


    @Parcelize
    object Settings: Destination()

    @Parcelize
    object SettingsNotifications: Destination()

    @Parcelize
    object SettingsAddresses: Destination()

    @Parcelize
    object SettingsAddressManipulation: Destination()

    @Parcelize
    data class SettingsAddressEditRemoval(val addressId: String): Destination()
}

class Actions(private val nav: Navigator<Destination>) {

    val pressOnBack = {
        nav.back()
    }

    val goTo = { destination: Destination ->
        if (nav.current != destination) {
            if (destination == Destination.Home) { // Don't keep a backstack when going Home
                 nav.clearBackStack()
            }
            nav.navigate(destination)
        }
    }

    fun goToRecycleWebsite(context: Context) {
        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://www.recycleapp.be")
        })
    }

    fun goToSigmaDeltaWebsite(context: Context) {
        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://www.sigmadelta.be")
        })
    }
}