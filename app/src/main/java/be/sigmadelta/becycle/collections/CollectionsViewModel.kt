package be.sigmadelta.becycle.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.sigmadelta.becycle.common.analytics.AnalTag
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
        shouldNotFetch: Boolean = false,
    ) = viewModelScope.launch {
        collectionsRepository
            .searchUpcomingCollections(address, shouldNotFetch = shouldNotFetch)
            .collect {
            if (it !is Response.Loading) {
                analTracker.log(AnalTag.SEARCH_COLLECTIONS) {
                    param(
                        "state", when (it) {
                            is Response.Success -> "success"
                            is Response.Error -> "error"
                            is Response.Loading -> "loading"
                        }
                    )
                    param(
                        "value", when (it) {
                            is Response.Loading -> ""
                            is Response.Success -> {
                                "Upcoming: ${it.body.upcoming ?: 0}\nToday: ${it.body.today ?: 0}\n Tomorrow: ${it.body.tomorrow ?: 0}"
                            }
                            is Response.Error -> it.error?.localizedMessage ?: ""
                        }
                    )
                }
            }

            collectionsViewState.value = it.toViewState()
        }
    }

    fun removeCollections(address: Address) = viewModelScope.launch {
        collectionsRepository.removeCollections(address)
        analTracker.log(AnalTag.REMOVE_COLLECTIONS) {
            param("address", address.fullAddress)
        }
    }
}
