package be.sigmadelta.becycle.baseheaders

import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                    analTracker.log(ANAL_TAG, bundleOf(
                        Pair("getBaseHeaders_success_secret", it.body.secret),
                        Pair("getBaseHeaders_success_consumer", it.body.consumer)
                    ))
                }
                is Response.Error -> {
                    baseHeadersViewState.value = BaseHeadersViewState.Error(it.error)
                    Napier.e("Error occurred: ${it.error}")
                    analTracker.log(ANAL_TAG, "getBaseHeaders_error", it.error?.message)

                }
            }
        }
    }

    companion object {
        private const val TAG = "BaseHeadersViewModel"
        private const val ANAL_TAG = "BaseHeadersVM"
    }
}

sealed class BaseHeadersViewState {
    object Empty: BaseHeadersViewState()
    object Loading: BaseHeadersViewState()
    data class Headers(val headers: List<Header>) : BaseHeadersViewState()
    data class Error(val error: Throwable?): BaseHeadersViewState()
}