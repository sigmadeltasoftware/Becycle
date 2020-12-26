package be.sigmadelta.becycle.accesstoken

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.sigmadelta.becycle.common.analytics.AnalTag
import be.sigmadelta.becycle.common.analytics.AnalyticsTracker
import be.sigmadelta.becycle.common.ui.util.ViewState
import be.sigmadelta.becycle.common.ui.util.toViewState
import be.sigmadelta.common.accesstoken.AccessTokenRepository
import be.sigmadelta.common.accesstoken.AccessToken
import be.sigmadelta.common.util.Response
import be.sigmadelta.common.util.toAnalStateType
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

            if (it !is Response.Loading) {
                analTracker.log(AnalTag.GET_ACCESS_TOKEN) {
                    param("state", it.toAnalStateType())

                    if (it is Response.Error) {
                        param("response", it.error?.localizedMessage ?: "")
                    }
                }
            }

            accessTokenViewState.value = it.toViewState()
        }
    }
}
