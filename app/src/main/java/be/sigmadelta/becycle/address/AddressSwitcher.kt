package be.sigmadelta.becycle.address

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import be.sigmadelta.becycle.R
import be.sigmadelta.becycle.common.ui.theme.*
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.becycle.common.ui.widgets.BecycleProgressIndicator
import be.sigmadelta.becycle.common.util.AmbientAddress
import be.sigmadelta.becycle.common.util.AmbientTabIndex
import be.sigmadelta.becycle.common.util.str
import com.github.aakira.napier.Napier

@Composable
fun AddressSwitcher(
    onGoToAddressInput: () -> Unit,
    onTabSelected: (Int) -> Unit
) {

    val selectedTabIx = AmbientTabIndex.current

    TabRow(
        selectedTabIndex = selectedTabIx,
        backgroundColor = primaryBackgroundColor,
        contentColor = primaryAccent,
        divider = { Divider() }
    ) {
        when (val addresses = AmbientAddress.current) {
            is ListViewState.Loading -> BecycleProgressIndicator(modifier = Modifier.padding(16.dp))
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
                            fontSize = titleFontSize,
                            color = if (selectedTabIx == ix) primaryAccent else unselectedColor
                        )
                        Text(
                            "${it.street.names.nl} ${it.houseNumber}",
                            fontSize = subTextFontSize,
                            color = if (selectedTabIx == ix) primaryAccent else unselectedColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (addresses.payload.size < 2) {
                    Tab(selected = false, onClick = onGoToAddressInput) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = R.string.add_address.str(),
                                fontWeight = FontWeight.Bold,
                                fontSize = regularFontSize,
                                color = unselectedColor
                            )
                            Icon(
                                imageVector = vectorResource(id = R.drawable.ic_add),
                                tint = primaryAccent,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
            is ListViewState.Error -> {
                Napier.e( addresses.error?.localizedMessage.toString())
                Text("Unable to retrieve address data", color = errorColor)
            }
        }
    }
}