package be.sigmadelta.becycle.address

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.becycle.common.util.AmbientAddress
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.address.Street
import be.sigmadelta.common.address.ZipCodeItem

@ExperimentalMaterialApi
@Composable
fun SettingsAddressEditRemoval(
    addressId: String,
    zipCodeItemViewState: ListViewState<ZipCodeItem>,
    streetsViewState: ListViewState<Street>,
    actions: SettingsAddressEditRemovalActions
) {

    when (val addresses = AmbientAddress.current) {
        is ListViewState.Error -> TODO()

        is ListViewState.Success -> {
            addresses.payload.firstOrNull { it.id == addressId }?.let {
                Column {
                    SettingsAddressManipulation(
                        zipCodeItemsViewState = zipCodeItemViewState,
                        streetsViewState = streetsViewState,
                        SettingsAddressManipulationActions(
                            onSearchZipCode = actions.onSearchZipCode,
                            onSearchStreet = actions.onSearchStreet,
                            onValidateAddress = { zipCodeItem, street, houseNumber ->
                                actions.onAddressChanged(
                                    it.copy(
                                        zipCodeItem = zipCodeItem,
                                        street = street,
                                        houseNumber = houseNumber
                                    )
                                )
                            },
                            onAddressRemove = actions.onAddressRemove,
                            onBackClicked = actions.onBackClicked
                        ),
                        "Edit Address",
                        it,
                    )
                }
            }
        }
    }
}

data class SettingsAddressEditRemovalActions (
    val onSearchZipCode: (String) -> Unit,
    val onSearchStreet: (String, ZipCodeItem) -> Unit,
    val onAddressChanged: (Address) -> Unit,
    val onAddressRemove: (Address) -> Unit,
    val onBackClicked: () -> Unit
)