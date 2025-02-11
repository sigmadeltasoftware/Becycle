package be.sigmadelta.becycle.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import be.sigmadelta.becycle.R
import be.sigmadelta.becycle.common.ui.theme.*
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.becycle.common.ui.widgets.BecycleProgressIndicator
import be.sigmadelta.becycle.common.util.AmbientAddress
import be.sigmadelta.becycle.common.util.str
import be.sigmadelta.common.Faction
import be.sigmadelta.common.address.Address

@Composable
fun SettingsAddressOverview(
    actions: AddressOverviewActions
) {
    var addressCount by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(R.string.settings__address.str())
                        Spacer(modifier = Modifier.weight(1f))
                        if (addressCount != 0) {
                            Text(
                                text = "$addressCount/5",
                                modifier = Modifier.background(
                                    color = if (addressCount < 5) secondaryAccent else warningColor,
                                    shape = RoundedCornerShape(8.dp)
                                ).padding(vertical = 4.dp, horizontal = 8.dp),
                                color = if (addressCount < 5) primaryAccent else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = subTextFontSize
                            )
                        }
                    }
                },
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
            when (val addresses = AmbientAddress.current) {
                is ListViewState.Empty -> Unit
                is ListViewState.Loading -> BecycleProgressIndicator()
                is ListViewState.Success -> Column {
                    addressCount = addresses.payload.size
                    LazyColumn {
                        itemsIndexed(addresses.payload) { ix, addr ->
                            SettingsAddressOverviewItem(addr, actions.onEditAddressClicked)
                            if (ix < addresses.payload.size - 1) {
                                Divider()
                            }
                        }
                    }
                    AddAddressItem(onAddAddressClicked = { actions.onAddAddressClicked(Faction.RECAPP) })
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
                text = address.streetWithHouseNr,
                color = textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = regularFontSize
            )
            Text(
                text = "${address.zipCode} ${address.municipality}",
                color = textPrimary,
                fontSize = subTextFontSize
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = vectorResource(id = R.drawable.ic_edit),
            modifier = Modifier.padding(32.dp),
            tint = unselectedColor
        )
    }
}

@Composable
fun AddAddressItem(onAddAddressClicked: (Faction) -> Unit) {
    Button(
        onClick = { onAddAddressClicked(Faction.RECAPP) },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = vectorResource(id = R.drawable.ic_add))
            Text(
                text = R.string.add_address.str(),
                modifier = Modifier.padding(start = 16.dp),
                fontSize = regularFontSize
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

data class AddressOverviewActions(
    val onEditAddressClicked: (Address) -> Unit,
    val onAddAddressClicked: (Faction) -> Unit,
    val onBackClicked: () -> Unit
)