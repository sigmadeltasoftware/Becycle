package be.sigmadelta.becycle.address

import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import be.sigmadelta.becycle.R
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.common.address.Address

@Composable
fun AddressOverview(
    addresses: ListViewState<Address>,
    onEditAddressClicked: () -> Unit,
    onAddAddressClicked: () -> Unit
) {
    when (addresses) {
        is ListViewState.Empty -> Unit
        is ListViewState.Loading -> CircularProgressIndicator()
        is ListViewState.Success -> Column {
            LazyColumnForIndexed(items = addresses.payload) { ix, addr ->
                AddressOverviewItem(index = ix, addresses = addresses.payload, onEditAddressClicked)
                if (ix < addresses.payload.size - 1) {
                    Divider()
                }
            }
            AddAddressItem(onAddAddressClicked = onAddAddressClicked)
        }
        is ListViewState.Error -> Text(text = "ERROR: ${addresses.error?.localizedMessage}")
    }
}

@Composable
fun AddressOverviewItem(
    index: Int,
    addresses: List<Address>,
    onEditAddressClicked: () -> Unit
) {
    val address = addresses[index]
    Row {
        Column(modifier = Modifier.clickable(onClick = onEditAddressClicked)) {
            Text(text = address.id)
            Text(text = "${address.street.names.nl} ${address.houseNumber}")
            Text(text = "${address.zipCodeItem.code} ${address.zipCodeItem.names.firstOrNull()?.nl}")
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(asset = vectorResource(id = R.drawable.ic_edit), modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun AddAddressItem(onAddAddressClicked: () -> Unit) {
    Card(modifier = Modifier.padding(16.dp).clickable(onClick = onAddAddressClicked)) {
        Column {
            Icon(asset = vectorResource(id = R.drawable.ic_home))
            Text(text = "Add address")
        }
    }
}