package be.sigmadelta.becycle.address

import android.widget.Space
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.*
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.sigmadelta.becycle.R
import be.sigmadelta.becycle.common.ui.theme.primaryAccent
import be.sigmadelta.becycle.common.ui.theme.secondaryAccent
import be.sigmadelta.becycle.common.ui.theme.textPrimary
import be.sigmadelta.becycle.common.ui.theme.unselectedColor
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.common.address.Address

@Composable
fun SettingsAddressOverview(
    addresses: ListViewState<Address>,
    onEditAddressClicked: (Address) -> Unit,
    onAddAddressClicked: () -> Unit
) {
    when (addresses) {
        is ListViewState.Empty -> Unit
        is ListViewState.Loading -> CircularProgressIndicator()
        is ListViewState.Success -> Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Addresses",
                    color = textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${addresses.payload.size}/5",
                    modifier = Modifier.background(secondaryAccent).padding(vertical = 8.dp, horizontal = 16.dp),
                    color = primaryAccent,
                    fontWeight = FontWeight.Bold,
                )
            }
            LazyColumnForIndexed(items = addresses.payload) { ix, addr ->
                SettingsAddressOverviewItem(addr, onEditAddressClicked)
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
fun SettingsAddressOverviewItem(
    address: Address,
    onEditAddressClicked: (Address) -> Unit
) {
    Row(modifier = Modifier.clickable(onClick = { onEditAddressClicked(address) })) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterVertically)
        )
        {
            Text(
                text = "${address.street.names.nl} ${address.houseNumber}",
                color = textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "${address.zipCodeItem.code} ${address.zipCodeItem.names.firstOrNull()?.nl}",
                color = textPrimary,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            asset = vectorResource(id = R.drawable.ic_edit),
            modifier = Modifier.padding(32.dp),
            tint = unselectedColor
        )
    }
}

@Composable
fun AddAddressItem(onAddAddressClicked: () -> Unit) {
    Button(
        onClick = onAddAddressClicked,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        Row(modifier = Modifier.align(Alignment.CenterVertically)) {
            Icon(asset = vectorResource(id = R.drawable.ic_add))
            Text(text = "Add address", modifier = Modifier.padding(start = 16.dp))
        }
    }
}