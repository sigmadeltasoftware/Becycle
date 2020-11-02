package be.sigmadelta.becycle.address

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.ExperimentalFocus
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.address.Street
import be.sigmadelta.common.address.ZipCodeItem

@ExperimentalFocus
@Composable
fun SettingsAddressEditRemoval(
    addressId: String,
    addresses: ListViewState<Address>,
    zipCodeItemViewState: ListViewState<ZipCodeItem>,
    streetsViewState: ListViewState<Street>,
    onSearchZipCode: (String) -> Unit,
    onSearchStreet: (String, ZipCodeItem) -> Unit,
    onAddressChanged: (Address) -> Unit,
    onAddressRemove: (Address) -> Unit,
    onBackClicked: () -> Unit
) {
    when (addresses) {
        is ListViewState.Error -> TODO()

        is ListViewState.Success -> {
            addresses.payload.firstOrNull { it.id == addressId }?.let {
                Column {
                    SettingsAddressManipulation(
                        zipCodeItemsViewState = zipCodeItemViewState,
                        streetsViewState = streetsViewState,
                        SettingsAddressManipulationActions(
                            onSearchZipCode = onSearchZipCode,
                            onSearchStreet = onSearchStreet,
                            onValidateAddress = { zipCodeItem, street, houseNumber ->
                                onAddressChanged(
                                    it.copy(
                                        zipCodeItem = zipCodeItem,
                                        street = street,
                                        houseNumber = houseNumber
                                    )
                                )
                            },
                            onAddressRemove = onAddressRemove,
                            onBackClicked = onBackClicked
                        ),
                        "Edit Address",
                        it,
                    )
                }
            }
        }
    }
}