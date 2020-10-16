package be.sigmadelta.becycle.address

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.becycle.common.ui.util.toViewState
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.address.AddressRepository
import be.sigmadelta.common.address.Street
import be.sigmadelta.common.address.ZipCodeItem
import be.sigmadelta.common.notifications.NotificationRepo
import be.sigmadelta.common.util.InvalidAddressException
import be.sigmadelta.common.util.Response
import io.ktor.client.features.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class AddressViewModel(
    private val addressRepository: AddressRepository,
    private val notificationRepo: NotificationRepo
) : ViewModel() {

    val addressesViewState = MutableStateFlow<ListViewState<Address>>(ListViewState.Empty())
    val zipCodeItemsViewState = MutableStateFlow<ListViewState<ZipCodeItem>>(ListViewState.Empty())
    val streetsViewState = MutableStateFlow<ListViewState<Street>>(ListViewState.Empty())
    val validationViewState = MutableStateFlow<ValidationViewState>(ValidationViewState.Empty)

    init {
        loadSavedAddresses()
    }

    fun saveAddress(address: Address) = viewModelScope.launch {
        val addressExists = addressRepository.getAddresses().map { it.id }.contains(address.id)

        if (addressExists) {
            addressRepository.updateAddress(address.id, address)
        } else {
            addressRepository.insertAddress(address)
            createDefaultNotificationSettings(address)
        }

        loadSavedAddresses()
    }

    fun loadSavedAddresses() = viewModelScope.launch {
        val addresses = addressRepository.getAddresses()
        Log.d(TAG, "loadSavedAddresses(): $addresses")
        addressesViewState.value = ListViewState.Success(addresses)
    }

    fun clearAllAddresses() = viewModelScope.launch {
        addressRepository.removeAddresses()
        loadSavedAddresses()
    }

    fun searchZipCode(searchQuery: String) = viewModelScope.launch {
        addressRepository.searchZipCodes(searchQuery).collect {
            zipCodeItemsViewState.value = it.toViewState()
        }
    }

    fun searchStreets(searchQuery: String, zipCodeItem: ZipCodeItem) = viewModelScope.launch {
        addressRepository.searchStreets(searchQuery, zipCodeItem).collect {
            streetsViewState.value = it.toViewState()
        }
    }

    fun validateAddress(zipCodeItem: ZipCodeItem, street: Street, houseNumber: Int) = viewModelScope.launch {
        addressRepository.validateAddress(zipCodeItem, street, houseNumber).collect {
            validationViewState.value = when (it) {
                is Response.Loading -> ValidationViewState.Loading
                is Response.Success -> ValidationViewState.Success(it.body)
                is Response.Error -> when (it.error) {
                    is ClientRequestException -> ValidationViewState.InvalidCombination
                    is InvalidAddressException -> ValidationViewState.InvalidAddressSpecified
                    else -> ValidationViewState.NetworkError
                }
            }
        }
    }

    fun validateExistingAddress(address: Address) = viewModelScope.launch {
        addressRepository.validateExistingAddress(address).collect {
            validationViewState.value = when (it) {
                is Response.Loading -> ValidationViewState.Loading
                is Response.Success -> ValidationViewState.Success(it.body)
                is Response.Error -> when (it.error) {
                    is ClientRequestException -> ValidationViewState.InvalidCombination
                    is InvalidAddressException -> ValidationViewState.InvalidAddressSpecified
                    else -> ValidationViewState.NetworkError
                }
            }
        }
    }

    private fun createDefaultNotificationSettings(address: Address) {
        notificationRepo.insertDefaultNotificationProps(address)
    }

    companion object {
        private const val TAG = "AddressViewModel"
    }
}

sealed class ValidationViewState {
    object Empty: ValidationViewState()
    object Loading: ValidationViewState()
    data class Success(val address: Address): ValidationViewState()
    object InvalidCombination: ValidationViewState()
    object InvalidAddressSpecified: ValidationViewState()
    object NetworkError: ValidationViewState()
}