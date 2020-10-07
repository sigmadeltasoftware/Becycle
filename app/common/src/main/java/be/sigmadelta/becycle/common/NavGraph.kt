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
    object AddressInput: Destination()

    @Parcelize
    object RecycleWebsite: Destination()

    @Parcelize
    object Settings: Destination()
}

class Actions(private val nav: Navigator<Destination>) {

    val pressOnBack = {
        nav.back()
    }

    val goTo = { destination: Destination -> nav.navigate(destination) }

    fun goToRecycleWebsite(context: Context) {
        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://www.recycleapp.be")
        })
    }
}