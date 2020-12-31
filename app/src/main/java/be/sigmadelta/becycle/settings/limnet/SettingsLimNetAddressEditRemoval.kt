package be.sigmadelta.becycle.settings.limnet

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
import be.sigmadelta.common.address.limnet.LimNetHouseNumberDao
import be.sigmadelta.common.address.limnet.LimNetMunicipalityDao
import be.sigmadelta.common.address.limnet.LimNetStreetDao

@ExperimentalMaterialApi
@Composable
fun SettingsLimNetAddressEditRemoval(
    addressId: String,
    municipalityItemViewState: ListViewState<LimNetMunicipalityDao>,
    streetsViewState: ListViewState<LimNetStreetDao>,
    houseNumberViewState: ListViewState<LimNetHouseNumberDao>,
    actions: SettingsLimNetAddressEditRemovalActions
) {

    when (val addresses = AmbientAddress.current) {
        is ListViewState.Error -> Text(text = "Error", color = errorColor)

        is ListViewState.Success -> {
            addresses.payload.firstOrNull { it.id == addressId }?.let {
                Column {
                    SettingsLimNetAddressManipulation(
                        municipalityItemsViewState = municipalityItemViewState,
                        streetsViewState = streetsViewState,
                        houseNumbersViewState = houseNumberViewState,
                        actions = SettingsLimNetAddressManipulationActions(
                            onAddressRemove = actions.onAddressRemove,
                            onBackClicked = actions.onBackClicked,
                            onSearchLimNetMunicipality = actions.onSearchLimNetMunicipality,
                            onSearchLimNetStreet = actions.onSearchLimNetStreet,
                            onSearchLimNetHouseNumbers = actions.onSearchLimNetHouseNumbers,
                            onSaveLimNetAddress = actions.onSaveLimNetAddress,
                            onExit = actions.onExit
                        ),
                        R.string.edit_address.str(),
                        it,
                    )
                }
            }
        }
    }
}

data class SettingsLimNetAddressEditRemovalActions (
    val onAddressRemove: (Address) -> Unit,
    val onBackClicked: () -> Unit,
    val onSearchLimNetMunicipality: (String) -> Unit,
    val onSearchLimNetStreet: (String, LimNetMunicipalityDao) -> Unit,
    val onSearchLimNetHouseNumbers: (String, LimNetStreetDao) -> Unit,
    val onSaveLimNetAddress: (LimNetMunicipalityDao, LimNetStreetDao, LimNetHouseNumberDao) -> Unit,
    val onExit: () -> Unit
)