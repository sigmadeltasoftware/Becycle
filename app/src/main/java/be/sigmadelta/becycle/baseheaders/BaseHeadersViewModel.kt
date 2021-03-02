package be.sigmadelta.becycle.baseheaders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.sigmadelta.becycle.common.analytics.AnalTag
import be.sigmadelta.becycle.common.analytics.AnalyticsTracker
import be.sigmadelta.common.util.Header
import be.sigmadelta.common.util.Response
import be.sigmadelta.common.baseheader.BaseHeadersRepository
import com.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class BaseHeadersViewModel(
    private val baseHeadersRepo: BaseHeadersRepository,
    private val analTracker: AnalyticsTracker
) : ViewModel() {

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
                    Napier.d("Positive response: ${it.body}")
                    analTracker.log(AnalTag.GET_BASE_HEADERS.s()){
                        param("secret", it.body.secret)
                        param("consumer", it.body.consumer)
                    }
                }
                is Response.Error -> {
                    baseHeadersViewState.value = BaseHeadersViewState.Error(it.error)
                    Napier.e("Error occurred: ${it.error}")
                    analTracker.log(AnalTag.GET_BASE_HEADERS.s()) {
                        param("error", it.error?.message ?: "")
                    }
                }
            }
        }
    }
}

sealed class BaseHeadersViewState {
    object Empty: BaseHeadersViewState()
    object Loading: BaseHeadersViewState()
    data class Headers(val headers: List<Header>) : BaseHeadersViewState()
    data class Error(val error: Throwable?): BaseHeadersViewState()
}