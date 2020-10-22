package be.sigmadelta.becycle.address

import android.util.Log
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.sigmadelta.becycle.R
import be.sigmadelta.becycle.common.ui.theme.*
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.common.address.Address

@Composable
fun AddressSwitcher(
    selectedTabIx: Int,
    addresses: ListViewState<Address>,
    onGoToAddressInput: () -> Unit,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTabIx,
        backgroundColor = primaryBackgroundColor,
        contentColor = primaryAccent,
        divider = { Divider() }
    ) {
        when (addresses) {
            is ListViewState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            is ListViewState.Success -> {
                addresses.payload.forEachIndexed { ix, it ->
                    Tab(
                        selected = selectedTabIx == ix,
                        onClick = { onTabSelected(ix) },
                        modifier = Modifier
                            .background(if (selectedTabIx == ix) secondaryAccent else primaryBackgroundColor)
                            .padding(16.dp),
                        selectedContentColor = primaryAccent,
                        unselectedContentColor = primaryBackgroundColor,
                    ) {
                        Text(
                            it.zipCodeItem.code,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIx == ix) primaryAccent else unselectedColor
                        )
                        Text(
                            "${it.street.names.nl} ${it.houseNumber}",
                            fontSize = 10.sp,
                            color = if (selectedTabIx == ix) primaryAccent else unselectedColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (addresses.payload.size < 2) {
                    Tab(selected = false, onClick = onGoToAddressInput) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = "Add Address",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = unselectedColor
                            )
                            Icon(
                                asset = vectorResource(id = R.drawable.ic_add),
                                tint = primaryAccent,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
            is ListViewState.Error -> {
                Log.e("AddressSwitcher", addresses.error?.localizedMessage.toString())
                Text("Unable to retrieve address data", color = errorColor)
            }
        }
    }
}