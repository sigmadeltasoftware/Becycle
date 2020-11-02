package be.sigmadelta.becycle.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.sigmadelta.becycle.common.analytics.AnalyticsTracker
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.becycle.common.ui.util.ViewState
import be.sigmadelta.becycle.common.ui.util.toViewState
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.collections.Collection
import be.sigmadelta.common.collections.CollectionOverview
import be.sigmadelta.common.collections.CollectionsRepository
import be.sigmadelta.common.util.Response
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class CollectionsViewModel(
    private val collectionsRepository: CollectionsRepository,
    private val analTracker: AnalyticsTracker
) : ViewModel() {

    val collectionsViewState = MutableStateFlow<ViewState<CollectionOverview>>(ViewState.Empty())

    fun searchCollections(
        address: Address,
    ) = viewModelScope.launch {
        collectionsRepository.searchUpcomingCollections(address).collect {
            analTracker.log(ANAL_TAG,
                "searchCollections_${when (it) {
                    is Response.Success -> "success"
                    is Response.Error -> "error"
                    is Response.Loading -> "loading"
                }}",
                when(it){
                    is Response.Loading -> null
                    is Response.Success -> {
                        "Upcoming: ${it.body.upcoming ?: 0}\nToday: ${it.body.today ?: 0}\n Tomorrow: ${it.body.tomorrow ?: 0}"
                    }
                    is Response.Error -> it.error?.localizedMessage
                })

            collectionsViewState.value = it.toViewState()
        }
    }

    fun removeCollections(address: Address) = viewModelScope.launch {
        collectionsRepository.removeCollections(address)
    }

    companion object {
        private const val TAG = "CollectionsViewModel"
        private const val ANAL_TAG = "CollectionsVM"
    }
}
