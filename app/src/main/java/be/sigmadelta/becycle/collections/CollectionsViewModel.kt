package be.sigmadelta.becycle.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.sigmadelta.becycle.common.analytics.AnalTag
import be.sigmadelta.becycle.common.analytics.AnalyticsTracker
import be.sigmadelta.becycle.common.ui.util.ViewState
import be.sigmadelta.becycle.common.ui.util.toViewState
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.collections.CollectionOverview
import be.sigmadelta.common.collections.CollectionsRepository
import be.sigmadelta.common.util.Response
import be.sigmadelta.common.util.toAnalStateType
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
                    param("state", it.toAnalStateType())
                    param(
                        "value", when (it) {
                            is Response.Loading -> ""
                            is Response.Success -> {
                                "Upcoming Size: ${it.body.upcoming?.size ?: 0}" +
                                "\nToday Size: ${it.body.today?.size ?: 0}" +
                                "\n Tomorrow Size: ${it.body.tomorrow?.size ?: 0}"
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
        analTracker.log(AnalTag.REMOVE_COLLECTIONS)
    }
}
