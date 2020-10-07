package be.sigmadelta.becycle.baseheaders

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.sigmadelta.common.util.Header
import be.sigmadelta.common.util.Response
import be.sigmadelta.common.baseheader.BaseHeadersRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class BaseHeadersViewModel(private val baseHeadersRepo: BaseHeadersRepository) : ViewModel() {

    val baseHeadersViewState = MutableStateFlow<BaseHeadersViewState?>(BaseHeadersViewState.Empty)

    fun getBaseHeaders() = viewModelScope.launch {
        baseHeadersRepo.observeBaseHeaders().collect {
            when (it) {
                is Response.Loading -> baseHeadersViewState.value = BaseHeadersViewState.Loading
                is Response.Success -> {
                    baseHeadersViewState.value = BaseHeadersViewState.Headers(
                        listOf(
                            Header("x-secret", it.body.secret),
                            Header("x-consumer", it.body.consumer)
                        )
                    )
                    Log.d("Becycle", "Positive response: ${it.body}")
                }
                is Response.Error -> {
                    baseHeadersViewState.value = BaseHeadersViewState.Error(it.error?.message)
                    Log.d("Becycle", "Error occurred: ${it.error}")
                }
            }
        }
    }
}

sealed class BaseHeadersViewState() {
    object Empty: BaseHeadersViewState()
    object Loading: BaseHeadersViewState()
    data class Headers(val headers: List<Header>) : BaseHeadersViewState()
    data class Error(val msg: String?): BaseHeadersViewState()
}