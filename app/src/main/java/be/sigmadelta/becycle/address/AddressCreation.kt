package be.sigmadelta.becycle.address

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.ExperimentalFocus
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.becycle.common.ui.widgets.DropDownTextField
import be.sigmadelta.common.address.Street
import be.sigmadelta.common.address.ZipCodeItem
import org.koin.ext.isInt

@ExperimentalFocus
@Composable
fun AddressCreation(
    zipCodeItemsViewState: ListViewState<ZipCodeItem>,
    streetsViewState: ListViewState<Street>,
    onSearchZipCode: (String) -> Unit,
    onSearchStreet: (String, ZipCodeItem) -> Unit,
    onValidateAddress: (ZipCodeItem, Street, Int) -> Unit,
    prefill: AddressCreationPrefill? = null,
    ) {
    var selectedZipCode by remember { mutableStateOf<ZipCodeItem?>(null) }
    var selectedStreet by remember { mutableStateOf<Street?>(null) }
    var selectedHouseNumber by remember { mutableStateOf("1") }

    prefill?.let { if (it.houseNumber.isInt()) selectedHouseNumber = it.houseNumber }

    val streetFocusRequester = FocusRequester()
    val houseNumberFocusRequester = FocusRequester()

    Column {
        DropDownTextField(
            zipCodeItemsViewState,
            textValue = selectedZipCode?.code ?: prefill?.postCodeNumber,
            label = "Zipcode",
            textChangeAction = {
                selectedZipCode = null
                onSearchZipCode(it)
            },
            itemSelectedAction = { zipCode ->
                selectedZipCode = zipCode
                streetFocusRequester.requestFocus()
            },
            itemLayout = { item: ZipCodeItem -> ZipCodeItemLayout(zipCodeItem = item) },
        )

        DropDownTextField(
            streetsViewState,
            textValue = selectedStreet?.names?.nl ?: prefill?.streetName,
            label = "Street",
            textChangeAction = {
                selectedStreet = null
                selectedZipCode?.let { zip ->
                    onSearchStreet(it, zip)
                }
            },
            itemSelectedAction = { street ->
                selectedStreet = street
                houseNumberFocusRequester.requestFocus()
            },
            itemLayout = { item: Street -> StreetLayout(street = item) },
            focusRequester = streetFocusRequester
        )

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(houseNumberFocusRequester)
                .padding(horizontal = 32.dp, vertical = 8.dp),
            backgroundColor = Color.White,
            label = { Text("House number") },
            value = selectedHouseNumber,
            onValueChange = {
                if (it.isInt()) {
                    selectedHouseNumber = it
                }
            },
            isErrorValue = !selectedHouseNumber.isInt()
        )

        Button(
            onClick = { onValidateAddress(selectedZipCode!!, selectedStreet!!, selectedHouseNumber.toInt()) },
            modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally),
            enabled = selectedZipCode != null && selectedStreet != null && selectedHouseNumber.isInt()
        ) {
            Text("Save Address")
        }
    }
}

@Composable
fun ZipCodeItemLayout(zipCodeItem: ZipCodeItem) {
    Text(zipCodeItem.code, fontWeight = FontWeight.Bold)
    Text(
        zipCodeItem.names.firstOrNull()?.nl + if (zipCodeItem.city.name == zipCodeItem.names.firstOrNull()?.nl) "" else " (${zipCodeItem.city.name})",
        fontSize = 10.sp,
    )
    Divider(modifier = Modifier.fillMaxWidth())
}

@Composable
fun StreetLayout(street: Street) {
    Text(street.names.nl, fontWeight = FontWeight.Bold)
    Text(street.id, fontSize = 10.sp)
    Divider(modifier = Modifier.fillMaxWidth())
}

data class AddressCreationPrefill(
    val postCodeNumber: String,
    val streetName: String,
    val houseNumber: String
)