package be.sigmadelta.becycle.accesstoken

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.sigmadelta.becycle.common.ui.util.ViewState
import be.sigmadelta.becycle.common.ui.util.toViewState
import be.sigmadelta.common.accesstoken.AccessTokenRepository
import be.sigmadelta.common.accesstoken.AccessToken
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class AccessTokenViewModel(private val accessTokenRepository: AccessTokenRepository) : ViewModel() {

    val accessTokenViewState = MutableStateFlow<ViewState<AccessToken>>(ViewState.Empty())

    fun getAccessToken() = viewModelScope.launch {
        accessTokenRepository.getAccessToken().collect {
            accessTokenViewState.value = it.toViewState()
        }
    }

    companion object {
        const val TAG = "RecycleViewModel"
    }
}
