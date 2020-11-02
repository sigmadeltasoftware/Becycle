package be.sigmadelta.becycle.address

import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
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
import be.sigmadelta.becycle.common.ui.widgets.BecycleProgressIndicator
import be.sigmadelta.common.address.Address

@Composable
fun SettingsAddressOverview(
    addresses: ListViewState<Address>,
    onEditAddressClicked: (Address) -> Unit,
    onAddAddressClicked: () -> Unit,
    onBackClicked: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Address Settings") },
                navigationIcon = {
                    Icon(
                        asset = vectorResource(id = R.drawable.ic_back),
                        modifier = Modifier.clickable(onClick = onBackClicked).padding(start = 8.dp)
                    )
                }
            )
        },
        bodyContent = {
            when (addresses) {
                is ListViewState.Empty -> Unit
                is ListViewState.Loading -> BecycleProgressIndicator()
                is ListViewState.Success -> Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp).padding(end = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${addresses.payload.size}/5",
                            modifier = Modifier.background(
                                color = secondaryAccent,
                                shape = RoundedCornerShape(16.dp)
                            ).padding(vertical = 4.dp, horizontal = 8.dp),
                            color = primaryAccent,
                            fontWeight = FontWeight.Bold
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
    )
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
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(asset = vectorResource(id = R.drawable.ic_add))
            Text(text = "Add Address", modifier = Modifier.padding(start = 16.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}