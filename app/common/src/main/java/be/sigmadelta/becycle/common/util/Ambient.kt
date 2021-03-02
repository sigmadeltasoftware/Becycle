package be.sigmadelta.becycle.common.util

import androidx.compose.runtime.staticAmbientOf
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.common.address.Address

val AmbientAddress = staticAmbientOf<ListViewState<Address>> {
    error("No AmbientAddress provided")
}

val AmbientTabIndex = staticAmbientOf<Int> {
    error("No AmbientTabIndex provided")
}