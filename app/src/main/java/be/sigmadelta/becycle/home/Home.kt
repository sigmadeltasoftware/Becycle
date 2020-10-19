package be.sigmadelta.becycle.home

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.sigmadelta.becycle.collections.Collections
import be.sigmadelta.becycle.common.Destination
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.becycle.notification.AddressSwitcher
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.collections.Collection

@Composable
fun Home(
    addresses: ListViewState<Address>,
    collections: ListViewState<Collection>,
    onGoToAddressInput: (Destination.SettingsAddressCreation) -> Unit,
    onLoadCollections: (Address) -> Unit
) {
    var isInitialized by remember { mutableStateOf(false) }

    when (addresses) {

        is ListViewState.Success -> if (addresses.payload.isEmpty()) {
            onGoToAddressInput(Destination.SettingsAddressCreation)
        } else {
            var selectedTabIx by remember { mutableStateOf(0) }

            Column {
                AddressSwitcher(
                    selectedTabIx = selectedTabIx,
                    addresses = addresses,
                    onTabSelected = { ix ->
                        selectedTabIx = ix
                        onLoadCollections(addresses.payload[selectedTabIx])
                    }
                )
                HomeLayout(
                    collections
                )
            }

            if (isInitialized.not() && addresses.payload.firstOrNull() != null) {
                addresses.payload.firstOrNull()?.let { onLoadCollections(it) }
                isInitialized = true
            }
        }

        is ListViewState.Error, is ListViewState.Empty -> Text("Failed to retrieve addresses!") // TODO: Prettify
    }
}

@Composable
fun HomeLayout(
    collections: ListViewState<Collection>
) {

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        when (collections) {
            is ListViewState.Empty -> Unit
            is ListViewState.Loading -> Unit // TODO
            is ListViewState.Success -> Collections(collections = collections.payload)
            is ListViewState.Error -> Unit // TODO
        }
    }
}