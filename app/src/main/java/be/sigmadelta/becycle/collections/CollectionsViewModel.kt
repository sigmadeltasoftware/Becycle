package be.sigmadelta.becycle.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.becycle.common.ui.util.toViewState
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.collections.Collection
import be.sigmadelta.common.collections.CollectionsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class CollectionsViewModel(private val collectionsRepository: CollectionsRepository) : ViewModel() {

    val collectionsViewState = MutableStateFlow<ListViewState<Collection>>(ListViewState.Empty())

    fun searchCollections(
        address: Address,
        fromDate: String,
        untilDate: String
    ) = viewModelScope.launch {
        collectionsRepository.searchCollections(address, fromDate, untilDate).collect {
            collectionsViewState.value = it.toViewState()
        }
    }
    companion object {
        const val TAG = "CollectionsViewModel"
    }
}
