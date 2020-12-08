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
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.collections.CollectionOverview

@Composable
fun Home(
    addresses: ListViewState<Address>,
    collectionOverview: ViewState<CollectionOverview>,
    onGoToAddressInput: () -> Unit,
    onLoadCollections: (Address) -> Unit
) {
    var isInitialized by remember { mutableStateOf(false) }

    when (addresses) {
        is ListViewState.Success -> if (addresses.payload.isEmpty()) {
            onGoToAddressInput()
        } else {
            var selectedTabIx by remember { mutableStateOf(0) }

            Column {
                AddressSwitcher(
                    selectedTabIx = selectedTabIx,
                    addresses = addresses,
                    onGoToAddressInput = onGoToAddressInput,
                    onTabSelected = { ix ->
                        selectedTabIx = ix
                        onLoadCollections(addresses.payload[selectedTabIx])
                    }
                )
                HomeLayout(
                    collectionOverview,
                    addresses.payload[selectedTabIx]
                )
            }

            if (isInitialized.not() && addresses.payload.firstOrNull() != null) {
                addresses.payload.firstOrNull()?.let { onLoadCollections(it) }
                isInitialized = true
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