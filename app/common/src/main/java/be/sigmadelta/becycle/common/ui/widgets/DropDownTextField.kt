package be.sigmadelta.becycle.common.ui.widgets

import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.ExperimentalFocus
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import be.sigmadelta.becycle.common.ui.util.ListViewState

@ExperimentalFocus
@Composable
fun <T> DropDownTextField(
    itemListViewState: ListViewState<T>,
    textValue: String?,
    label: String = "",
    textChangeAction: (String) -> Unit,
    itemSelectedAction: (T) -> Unit,
    itemLayout: @Composable (LazyItemScope.(T) -> Unit),
    focusRequester: FocusRequester = FocusRequester()
) {
    var textField by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        TextField(
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            backgroundColor = Color.White,
            label = {
                if (itemListViewState !is ListViewState.Error) {
                    Text(text = label)
                } else {
                    Text(text = "Error occurred: ${itemListViewState.error?.localizedMessage}")
                }
            },
            value = textValue ?: textField,
            onValueChange = {
                textField = it
                if (it.isNotBlank() && it.length > 1) {
                    textChangeAction(it)
                }
            },
            isErrorValue = itemListViewState is ListViewState.Error,
        )
        if (textValue == null) {
            when (itemListViewState) {
                is ListViewState.Loading -> CircularProgressIndicator()
                is ListViewState.Success -> {
                    LazyColumnFor(
                        modifier = Modifier.preferredHeight(150.dp).padding(start = 16.dp)
                            .padding(vertical = 4.dp),
                        items = itemListViewState.payload,
                        itemContent = {
                            Column(modifier = Modifier.clickable(onClick = { itemSelectedAction(it) })) {
                                itemLayout(it)
                            }
                        }
                    )
                }
            }
        }
    }
}