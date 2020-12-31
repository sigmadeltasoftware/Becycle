package be.sigmadelta.becycle.settings.recapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AmbientContext
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
import be.sigmadelta.becycle.common.util.str
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.address.recapp.RecAppStreetDao
import be.sigmadelta.common.address.recapp.RecAppZipCodeItemDao
import com.afollestad.materialdialogs.MaterialDialog
import org.koin.ext.isInt

@ExperimentalMaterialApi
@Composable
fun SettingsRecAppAddressManipulation(
    zipCodeItemsViewState: ListViewState<RecAppZipCodeItemDao>,
    streetsViewState: ListViewState<RecAppStreetDao>,
    actions: SettingsRecAppAddressManipulationActions,
    appBarTitle: String? = null,
    existingAddress: Address? = null,
) {
    var selectedZipCode by remember { mutableStateOf<RecAppZipCodeItemDao?>(null) }
    var isInvalidZipCodeInput by remember { mutableStateOf(false) }
    var selectedStreet by remember { mutableStateOf<RecAppStreetDao?>(null) }
    var selectedHouseNumber by remember { mutableStateOf("") }

    val zipCodeFocusRequester = FocusRequester()
    val streetFocusRequester = FocusRequester()
    val houseNumberFocusRequester = FocusRequester()

    onDispose(callback = {
        actions.onExit()
    })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = appBarTitle ?: R.string.create_address.str()) },
                navigationIcon = {
                    Icon(
                        imageVector = vectorResource(id = R.drawable.ic_back),
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
                    label = R.string.zipcode.str(),
                    textChangeAction = {
                        selectedZipCode = null

                        if (it.isInt()) {
                            isInvalidZipCodeInput = false
                            if (it.toInt() in 35..39) {
                                actions.onLimNetZipCodeEntered()
                            } else {
                                actions.onSearchRecAppZipCode(it)
                            }
                        } else {
                            isInvalidZipCodeInput = true
                        }
                    },
                    itemSelectedAction = { zipCode ->
                        selectedZipCode = zipCode
                        streetFocusRequester.requestFocus()
                    },
                    itemLayout = { item: RecAppZipCodeItemDao -> ZipCodeItemLayout(zipCodeItem = item) },
                    focusRequester = zipCodeFocusRequester,
                    keyboardType = KeyboardType.Number,
                    isError = isInvalidZipCodeInput,
                    onActiveCallback = {
                        zipCodeFocusRequester.requestFocus()
                    }
                )

                DropDownTextField(
                    streetsViewState,
                    textValue = selectedStreet?.names?.nl,
                    label = R.string.street.str(),
                    textChangeAction = {
                        selectedStreet = null
                        selectedZipCode?.let { zip ->
                            actions.onSearchRecAppStreet(it, zip)
                        }
                    },
                    itemSelectedAction = { street ->
                        selectedStreet = street
                        houseNumberFocusRequester.requestFocus()
                    },
                    itemLayout = { item: RecAppStreetDao -> StreetLayout(street = item) },
                    focusRequester = streetFocusRequester,
                    isError = selectedZipCode == null
                )

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(houseNumberFocusRequester)
                        .padding(horizontal = 32.dp, vertical = 8.dp),
                    backgroundColor = Color.White,
                    label = { Text(R.string.house_number.str()) },
                    value = selectedHouseNumber,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = { number ->
                        selectedHouseNumber = number
                        actions.onHouseNumberValueChanged()
                    },
                    isErrorValue = !selectedHouseNumber.isInt()
                )

                Column(Modifier.padding(horizontal = 24.dp)) {
                    Button(
                        onClick = {
                            actions.onValidateRecAppAddress(
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
                        Text(R.string.save_address.str())
                    }

                    actions.onAddressRemove?.let {
                        val ctx = AmbientContext.current
                        Button(
                            modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(),
                            onClick = {
                                MaterialDialog(ctx).show {
                                    cornerRadius(16f)
                                    title(R.string.remove_address)
                                    message(R.string.remove_address__text)
                                    positiveButton(R.string.remove_address) {
                                        existingAddress?.let { address ->
                                            it(address)
                                        }
                                    }
                                    negativeButton(android.R.string.cancel) { it.dismiss() }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = errorColor)
                        ) {
                            Text(text = R.string.remove_address.str())
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ZipCodeItemLayout(zipCodeItem: RecAppZipCodeItemDao) {
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
fun StreetLayout(street: RecAppStreetDao) {
    Text(
        street.names.nl,
        modifier = Modifier.padding(vertical = 8.dp),
        fontWeight = FontWeight.Bold,
        fontSize = regularFontSize)
    Divider(modifier = Modifier.fillMaxWidth())
}

data class SettingsRecAppAddressManipulationActions(
    val onSearchRecAppZipCode: (String) -> Unit,
    val onSearchRecAppStreet: (String, RecAppZipCodeItemDao) -> Unit,
    val onValidateRecAppAddress: (RecAppZipCodeItemDao, RecAppStreetDao, Int) -> Unit,
    val onHouseNumberValueChanged: () -> Unit,
    val onAddressRemove: ((Address) -> Unit)?,
    val onLimNetZipCodeEntered: () -> Unit,
    val onBackClicked: () -> Unit,
    val onExit: () -> Unit
)