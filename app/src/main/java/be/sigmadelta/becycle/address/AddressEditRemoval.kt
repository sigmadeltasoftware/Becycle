package be.sigmadelta.becycle.address

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import be.sigmadelta.becycle.R
import be.sigmadelta.becycle.common.ui.theme.errorColor
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.becycle.common.util.AmbientAddress
import be.sigmadelta.becycle.common.util.str
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.address.RecAppAddressDao
import be.sigmadelta.common.address.recapp.RecAppStreetDao
import be.sigmadelta.common.address.recapp.RecAppZipCodeItemDao

@ExperimentalMaterialApi
@Composable
fun SettingsRecAppAddressEditRemoval(
    addressId: String,
    zipCodeItemViewState: ListViewState<RecAppZipCodeItemDao>,
    streetsViewState: ListViewState<RecAppStreetDao>,
    actions: SettingsAddressEditRemovalActions
) {

    when (val addresses = AmbientAddress.current) {
        is ListViewState.Error -> Text(text = "Error", color = errorColor)

        is ListViewState.Success -> {
            addresses.payload.firstOrNull { it.id == addressId }?.let {
                Column {
                    SettingsAddressManipulation(
                        zipCodeItemsViewState = zipCodeItemViewState,
                        streetsViewState = streetsViewState,
                        SettingsAddressManipulationActions(
                            onSearchZipCode = actions.onSearchZipCode,
                            onSearchStreet = actions.onSearchStreet,
                            onHouseNumberValueChanged = actions.onHouseNumberValueChanged,
                            onValidateAddress = { zipCodeItem, street, houseNumber ->
                                actions.onAddressChanged(
                                    RecAppAddressDao(
                                        id = it.id,
                                        zipCodeItem = zipCodeItem,
                                        street = street,
                                        houseNumber = houseNumber
                                    )
                                )
                            },
                            onAddressRemove = actions.onAddressRemove,
                            onBackClicked = actions.onBackClicked
                        ),
                        R.string.edit_address.str(),
                        it,
                    )
                }
            }
        }
    }
}

data class SettingsAddressEditRemovalActions (
    val onSearchZipCode: (String) -> Unit,
    val onSearchStreet: (String, RecAppZipCodeItemDao) -> Unit,
    val onAddressChanged: (RecAppAddressDao) -> Unit,
    val onAddressRemove: (Address) -> Unit,
    val onHouseNumberValueChanged: () -> Unit,
    val onBackClicked: () -> Unit
)