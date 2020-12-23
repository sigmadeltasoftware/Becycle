package be.sigmadelta.becycle.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.sigmadelta.becycle.address.AddressSwitcher
import be.sigmadelta.becycle.collections.Collections
import be.sigmadelta.becycle.collections.EmptyCollections
import be.sigmadelta.becycle.common.ui.theme.bottomNavigationMargin
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.becycle.common.ui.util.ViewState
import be.sigmadelta.becycle.common.util.AmbientAddress
import be.sigmadelta.becycle.common.util.AmbientTabIndex
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.collections.CollectionOverview

@Composable
fun Home(
    collectionOverview: ViewState<CollectionOverview>,
    actions: HomeActions
) {
    var isInitialized by remember { mutableStateOf(false) }
    val selectedTabIx = AmbientTabIndex.current

    when (val addresses = AmbientAddress.current) {
        is ListViewState.Success -> if (addresses.payload.isEmpty()) {
            actions.onGoToAddressInput()
        } else {

            Column {
                AddressSwitcher(
                    onGoToAddressInput = actions.onGoToAddressInput,
                    onTabSelected = { ix -> actions.onTabSelected(ix) }
                )
                HomeLayout(
                    collectionOverview,
                    addresses.payload[selectedTabIx]
                )
            }

            if (isInitialized.not()) {
                isInitialized = true
                actions.onLoadCollections(addresses.payload[selectedTabIx])
            }
        }

        is ListViewState.Error -> Text("Failed to retrieve addresses!") // TODO: Prettify
    }
}

@Composable
fun HomeLayout(
    collectionOverview: ViewState<CollectionOverview>,
    address: Address
) {

    Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = bottomNavigationMargin + 8.dp)) {
        when (collectionOverview) {
            is ViewState.Empty -> EmptyCollections(address)
            is ViewState.Loading -> Unit // TODO
            is ViewState.Success -> Collections(collectionOverview = collectionOverview.payload)
            is ViewState.Error -> Unit // TODO
        }
    }
}

data class HomeActions(
    val onGoToAddressInput: () -> Unit,
    val onLoadCollections: (Address) -> Unit,
    val onTabSelected: (Int) -> Unit
)