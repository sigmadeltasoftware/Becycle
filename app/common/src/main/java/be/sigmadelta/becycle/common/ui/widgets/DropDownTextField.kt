package be.sigmadelta.becycle.common.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import be.sigmadelta.becycle.common.ui.util.ListViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun <T> DropDownTextField(
    itemListViewState: ListViewState<T>,
    textValue: String?,
    label: String = "",
    textChangeAction: (String) -> Unit,
    itemSelectedAction: (T) -> Unit,
    itemLayout: @Composable (LazyItemScope.(T) -> Unit),
    keyboardType: KeyboardType = KeyboardType.Text,
    focusRequester: FocusRequester = FocusRequester(),
    isError: Boolean = false,
    onActiveCallback: (CommitScope.() -> Unit)? = null,
    minimumChars: Int = 2,
) {

    var textField by remember { mutableStateOf("") }
    var isDebouncing by remember { mutableStateOf(false) }
    var job = remember { mutableStateOf(newJob { isDebouncing = false }) }

    Column(modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)) {
        TextField(
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            backgroundColor = Color.White,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
                if (isDebouncing.not() && it.isNotBlank() && it.length > minimumChars-1) {
                    isDebouncing = true
                    textChangeAction(it)
                }

                // Debouncing routine
                if (job.value.isActive) {
                    job.value.cancel()
                }
                job.value = newJob { isDebouncing = false }.apply {
                    invokeOnCompletion {
                        // Solely execute the last search action when debouncing is done
                        if (isDebouncing.not() && textField.isNotBlank() && textField.length > 1) {
                            textChangeAction(textField)
                        }
                    }
                }
                job.value.start()
            },
            isErrorValue = itemListViewState is ListViewState.Error || isError,
        )
        if (textValue == null) {
            when (itemListViewState) {
                is ListViewState.Loading -> BecycleProgressIndicator()
                is ListViewState.Success -> {
                    LazyColumn(
                        modifier = Modifier.preferredHeight(150.dp).padding(start = 16.dp)
                            .padding(vertical = 4.dp),
                        content = {
                            items(itemListViewState.payload) {
                                Column(modifier = Modifier.clickable(onClick = {
                                    itemSelectedAction(
                                        it
                                    )
                                })) {
                                    itemLayout(it)
                                }
                            }
                        }
                    )
                }
                else -> Unit
            }
        }
        onActive(callback = onActiveCallback ?: {})
    }
}

private fun newJob(action: () -> Unit) = GlobalScope.launch(Dispatchers.IO) {
    delay(500L)
    action()
}