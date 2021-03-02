package be.sigmadelta.becycle.settings.limnet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.sigmadelta.becycle.R
import be.sigmadelta.becycle.common.ui.theme.*
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.becycle.common.ui.widgets.DropDownTextField
import be.sigmadelta.becycle.common.util.str
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.address.limnet.LimNetHouseNumberDao
import be.sigmadelta.common.address.limnet.LimNetMunicipalityDao
import be.sigmadelta.common.address.limnet.LimNetStreetDao
import com.afollestad.materialdialogs.MaterialDialog
import org.koin.ext.isInt

@ExperimentalMaterialApi
@Composable
fun SettingsLimNetAddressManipulation(
    municipalityItemsViewState: ListViewState<LimNetMunicipalityDao>,
    streetsViewState: ListViewState<LimNetStreetDao>,
    houseNumbersViewState: ListViewState<LimNetHouseNumberDao>,
    actions: SettingsLimNetAddressManipulationActions,
    appBarTitle: String? = null,
    existingAddress: Address? = null,
) {
    var isMunicipalityTextInvalid by remember { mutableStateOf(false) }
    var selectedMunicipality by remember { mutableStateOf<LimNetMunicipalityDao?>(null) }
    var selectedStreet by remember { mutableStateOf<LimNetStreetDao?>(null) }
    var selectedHouseNumber by remember { mutableStateOf<LimNetHouseNumberDao?>(null) }

    val municipalityFocusRequester = FocusRequester()
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                AndroidView(viewBlock = {
//                    ImageView(it).apply {
//                        setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_limburgnet))
//                    }
//                },
//                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 32.dp),
//                    update = {
//                        it.setImageDrawable(
//                            ContextCompat.getDrawable(it.context, R.drawable.ic_limburgnet)
//                        )
//                    }
//                )

                Text(
                    text = AmbientContext.current.getString(R.string.settings__addressmanipulation_limnet_intercommunal),
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center,
                    color = primaryAccent,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                DropDownTextField(
                    municipalityItemsViewState,
                    textValue = selectedMunicipality?.naam,
                    label = R.string.municipality.str(),
                    textChangeAction = {
                        isMunicipalityTextInvalid =
                            it.contains(Regex("[^A-Za-z]")) // Check if string contains anything other than A-Z
                        selectedMunicipality = null
                        actions.onSearchLimNetMunicipality(it)
                    },
                    itemSelectedAction = { municipality ->
                        selectedMunicipality = municipality
                        streetFocusRequester.requestFocus()
                    },
                    itemLayout = { item: LimNetMunicipalityDao -> ItemLayout(item.naam) },
                    focusRequester = municipalityFocusRequester,
                    keyboardType = KeyboardType.Text,
                    isError = isMunicipalityTextInvalid || municipalityItemsViewState is ListViewState.Error,
                    onActiveCallback = {
                        municipalityFocusRequester.requestFocus()
                    }
                )

                DropDownTextField(
                    streetsViewState,
                    textValue = selectedStreet?.naam,
                    label = R.string.street.str(),
                    textChangeAction = {
                        selectedStreet = null
                        selectedMunicipality?.let { zip ->
                            actions.onSearchLimNetStreet(it, zip)
                        }
                    },
                    itemSelectedAction = { street ->
                        selectedStreet = street
                        houseNumberFocusRequester.requestFocus()
                    },
                    itemLayout = { item: LimNetStreetDao -> ItemLayout(item.naam) },
                    focusRequester = streetFocusRequester,
                    isError = selectedMunicipality == null
                )

                DropDownTextField(
                    houseNumbersViewState,
                    textValue = selectedHouseNumber?.huisNummer,
                    label = R.string.house_number.str(),
                    textChangeAction = {
                        selectedHouseNumber = null

                        if (it.isInt()) {
                            selectedStreet?.let { street ->
                                actions.onSearchLimNetHouseNumbers(it, street)
                            }
                        }
                    },
                    itemSelectedAction = { houseNumber ->
                        selectedHouseNumber = houseNumber
                    },
                    itemLayout = { item: LimNetHouseNumberDao -> ItemLayout(item.huisNummer) },
                    focusRequester = houseNumberFocusRequester,
                    keyboardType = KeyboardType.Number,
                    isError = selectedMunicipality == null
                            || selectedStreet == null
                            || houseNumbersViewState is ListViewState.Error
                            || (houseNumbersViewState as? ListViewState.Success)?.payload?.isEmpty() == true,
                    minimumChars = 1
                )

                Column(Modifier.padding(horizontal = 24.dp)) {
                    Button(
                        onClick = {
                            actions.onSaveLimNetAddress(
                                selectedMunicipality!!,
                                selectedStreet!!,
                                selectedHouseNumber!!
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .align(Alignment.CenterHorizontally),
                        enabled = selectedMunicipality != null && selectedStreet != null && selectedHouseNumber != null
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
fun ItemLayout(text: String) {
    Text(
        text,
        modifier = Modifier.padding(vertical = 8.dp),
        fontWeight = FontWeight.Bold,
        fontSize = regularFontSize
    )
    Divider(modifier = Modifier.fillMaxWidth())
}

data class SettingsLimNetAddressManipulationActions(
    val onSearchLimNetStreet: (String, LimNetMunicipalityDao) -> Unit,
    val onSearchLimNetMunicipality: (String) -> Unit,
    val onSearchLimNetHouseNumbers: (String, LimNetStreetDao) -> Unit,
    val onSaveLimNetAddress: (LimNetMunicipalityDao, LimNetStreetDao, LimNetHouseNumberDao) -> Unit,
    val onExit: () -> Unit,
    val onAddressRemove: ((Address) -> Unit)?,
    val onBackClicked: () -> Unit
)