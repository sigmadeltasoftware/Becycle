package be.sigmadelta.becycle.address

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.ExperimentalFocus
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import be.sigmadelta.becycle.R
import be.sigmadelta.becycle.common.ui.theme.errorColor
import be.sigmadelta.becycle.common.ui.theme.regularFontSize
import be.sigmadelta.becycle.common.ui.theme.subTextFontSize
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.becycle.common.ui.widgets.DropDownTextField
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.address.Street
import be.sigmadelta.common.address.ZipCodeItem
import com.afollestad.materialdialogs.MaterialDialog
import org.koin.ext.isInt

@ExperimentalMaterialApi
@ExperimentalFocus
@Composable
fun SettingsAddressManipulation(
    zipCodeItemsViewState: ListViewState<ZipCodeItem>,
    streetsViewState: ListViewState<Street>,
    actions: SettingsAddressManipulationActions,
    appBarTitle: String? = null,
    existingAddress: Address? = null,
) {
    var selectedZipCode by remember { mutableStateOf<ZipCodeItem?>(null) }
    var isInvalidZipCodeInput by remember { mutableStateOf(false) }
    var selectedStreet by remember { mutableStateOf<Street?>(null) }
    var selectedHouseNumber by remember { mutableStateOf("") }

    val streetFocusRequester = FocusRequester()
    val houseNumberFocusRequester = FocusRequester()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = appBarTitle ?: "Create Address") },
                navigationIcon = {
                    Icon(
                        asset = vectorResource(id = R.drawable.ic_back),
                        modifier = Modifier.clickable(onClick = actions.onBackClicked)
                            .padding(start = 8.dp)
                    )
                }
            )
        },
        bodyContent = {
            Column {
                DropDownTextField(
                    zipCodeItemsViewState,
                    textValue = selectedZipCode?.code,
                    label = "Zipcode",
                    textChangeAction = {
                        selectedZipCode = null
                        if (it.isInt()) {
                            isInvalidZipCodeInput = false
                            actions.onSearchZipCode(it)
                        } else {
                            isInvalidZipCodeInput = true
                        }
                    },
                    itemSelectedAction = { zipCode ->
                        selectedZipCode = zipCode
                        streetFocusRequester.requestFocus()
                    },
                    itemLayout = { item: ZipCodeItem -> ZipCodeItemLayout(zipCodeItem = item) },
                    keyboardType = KeyboardType.Number,
                    isError = isInvalidZipCodeInput
                )

                DropDownTextField(
                    streetsViewState,
                    textValue = selectedStreet?.names?.nl,
                    label = "Street",
                    textChangeAction = {
                        selectedStreet = null
                        selectedZipCode?.let { zip ->
                            actions.onSearchStreet(it, zip)
                        }
                    },
                    itemSelectedAction = { street ->
                        selectedStreet = street
                        houseNumberFocusRequester.requestFocus()
                    },
                    itemLayout = { item: Street -> StreetLayout(street = item) },
                    focusRequester = streetFocusRequester,
                    isError = selectedZipCode == null
                )

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(houseNumberFocusRequester)
                        .padding(horizontal = 32.dp, vertical = 8.dp),
                    backgroundColor = Color.White,
                    label = { Text("House number") },
                    value = selectedHouseNumber,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = { number ->
                        selectedHouseNumber = number
                    },
                    isErrorValue = !selectedHouseNumber.isInt()
                )

                Column(Modifier.padding(horizontal = 24.dp)) {
                    Button(
                        onClick = {
                            actions.onValidateAddress(
                                selectedZipCode!!,
                                selectedStreet!!,
                                selectedHouseNumber.toInt()
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .align(Alignment.CenterHorizontally),
                        enabled = selectedZipCode != null && selectedStreet != null && selectedHouseNumber.isInt()
                    ) {
                        Text("Save Address")
                    }

                    actions.onAddressRemove?.let {
                        val ctx = ContextAmbient.current
                        Button(
                            modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(),
                            onClick = {
                                MaterialDialog(ctx).show {
                                    cornerRadius(16f)
                                    title(text = "Remove Address?")
                                    message(text = "Are you sure you want to remove this address?")
                                    positiveButton(text = "Remove") {
                                        existingAddress?.let { address ->
                                            it(address)
                                        }
                                    }
                                    negativeButton(text = "Cancel") { it.dismiss() }
                                }
                            },
                            colors = ButtonConstants.defaultButtonColors(backgroundColor = errorColor)
                        ) {
                            Text(text = "Remove Address")
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ZipCodeItemLayout(zipCodeItem: ZipCodeItem) {
    Text(
        zipCodeItem.code,
        fontWeight = FontWeight.Bold,
        fontSize = regularFontSize
    )
    Text(
        zipCodeItem.names.firstOrNull()?.nl + if (zipCodeItem.city.name == zipCodeItem.names.firstOrNull()?.nl) "" else " (${zipCodeItem.city.name})",
        fontSize = subTextFontSize,
    )
    Divider(modifier = Modifier.fillMaxWidth())
}

@Composable
fun StreetLayout(street: Street) {
    Text(street.names.nl, fontWeight = FontWeight.Bold, fontSize = regularFontSize)
    Text(street.id, fontSize = subTextFontSize)
    Divider(modifier = Modifier.fillMaxWidth())
}

data class AddressCreationPrefill(
    val zipCodeNumber: String,
    val streetName: String,
    val houseNumber: String
)

data class SettingsAddressManipulationActions(
    val onSearchZipCode: (String) -> Unit,
    val onSearchStreet: (String, ZipCodeItem) -> Unit,
    val onValidateAddress: (ZipCodeItem, Street, Int) -> Unit,
    val onAddressRemove: ((Address) -> Unit)?,
    val onBackClicked: () -> Unit
)