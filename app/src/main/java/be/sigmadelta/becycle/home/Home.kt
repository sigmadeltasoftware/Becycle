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
    onClearAddresses: () -> Unit,
    onLoadCollections: (Address) -> Unit
) {
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
                    collections,
                    addresses.payload,
                    selectedTabIx,
                    onClearAddresses,
                    onLoadCollections
                )
            }

        }

        is ListViewState.Error, is ListViewState.Empty -> Text("Failed to retrieve addresses!") // TODO: Prettify
    }
}

@Composable
fun HomeLayout(
    collections: ListViewState<Collection>,
    addresses: List<Address>,
    addressIndex: Int,
    onClearAddresses: () -> Unit,
    onLoadCollections: (Address) -> Unit
) {

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Button(
            onClick = { onClearAddresses() },
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
        ) {
            Text("Clear Address(es)")
        }
        Button(
            onClick = { onLoadCollections(addresses[addressIndex]) },
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
        ) {
            Text("Load collections")
        }
        when (collections) {
            is ListViewState.Empty -> Unit
            is ListViewState.Loading -> Unit // TODO
            is ListViewState.Success -> Collections(collections = collections.payload)
            is ListViewState.Error -> Unit // TODO
        }
    }
}