package be.sigmadelta.becycle.accesstoken

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.sigmadelta.becycle.common.analytics.AnalyticsTracker
import be.sigmadelta.becycle.common.ui.util.ViewState
import be.sigmadelta.becycle.common.ui.util.toViewState
import be.sigmadelta.common.accesstoken.AccessTokenRepository
import be.sigmadelta.common.accesstoken.AccessToken
import be.sigmadelta.common.util.Response
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class AccessTokenViewModel(
    private val accessTokenRepository: AccessTokenRepository,
    private val analTracker: AnalyticsTracker
) : ViewModel() {

    val accessTokenViewState = MutableStateFlow<ViewState<AccessToken>>(ViewState.Empty())

    fun getAccessToken() = viewModelScope.launch {
        accessTokenRepository.getAccessToken().collect {
            analTracker.log(
                ANAL_TAG,
                "getAccessToken.${when (it) {
                    is Response.Success -> "success"
                    is Response.Error -> "error"
                    is Response.Loading -> "loading"
                }}",
                when(it){
                    is Response.Loading -> null
                    is Response.Success -> it.body
                    is Response.Error -> it.error?.localizedMessage
                })

            accessTokenViewState.value = it.toViewState()
        }
    }

    companion object {
        const val TAG = "RecycleViewModel"
        private const val ANAL_TAG = "AccessTokenVM"
    }
}
