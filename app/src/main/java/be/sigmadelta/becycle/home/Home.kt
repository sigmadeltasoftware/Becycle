package be.sigmadelta.becycle.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.sigmadelta.becycle.address.AddressSwitcher
import be.sigmadelta.becycle.collections.CollectionActions
import be.sigmadelta.becycle.collections.Collections
import be.sigmadelta.becycle.collections.EmptyCollections
import be.sigmadelta.becycle.common.ui.theme.bottomNavigationMargin
import be.sigmadelta.becycle.common.ui.theme.errorColor
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.becycle.common.ui.util.ViewState
import be.sigmadelta.becycle.common.util.AmbientAddress
import be.sigmadelta.becycle.common.util.AmbientTabIndex
import be.sigmadelta.becycle.common.util.str
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.collections.CollectionOverview

@ExperimentalMaterialApi
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
                    addresses.payload[selectedTabIx],
                    actions
                )
            }

            if (isInitialized.not()) {
                isInitialized = true // Disregard linting remark, this is effectively necessary
                actions.onLoadCollections(addresses.payload[selectedTabIx])
            }
        }

        is ListViewState.Error -> Text(
            be.sigmadelta.becycle.R.string.retrieve_addresses__error.str(),
            color = errorColor
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun HomeLayout(
    collectionOverview: ViewState<CollectionOverview>,
    address: Address,
    actions: HomeActions
) {

    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
            .padding(bottom = bottomNavigationMargin + 8.dp)
    ) {
        when (collectionOverview) {
            is ViewState.Empty -> EmptyCollections(address)
            is ViewState.Loading -> Unit // TODO
            is ViewState.Success -> Collections(
                collectionOverview = collectionOverview.payload,
                actions = CollectionActions(
                    onSwipeToRefresh = {
                        actions.onLoadCollections(address)
                    }
                )
            )
            is ViewState.Error -> Unit // TODO
        }
    }
}

data class HomeActions(
    val onGoToAddressInput: () -> Unit,
    val onLoadCollections: (Address) -> Unit,
    val onTabSelected: (Int) -> Unit
)