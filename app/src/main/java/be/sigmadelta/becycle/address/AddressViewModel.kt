package be.sigmadelta.becycle.address

import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.sigmadelta.becycle.common.analytics.AnalTag
import be.sigmadelta.becycle.common.analytics.AnalyticsTracker
import be.sigmadelta.becycle.common.analytics.UserProps
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.becycle.common.ui.util.toViewState
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.address.AddressRepository
import be.sigmadelta.common.address.Street
import be.sigmadelta.common.address.ZipCodeItem
import be.sigmadelta.common.notifications.NotificationRepo
import be.sigmadelta.common.util.InvalidAddressException
import be.sigmadelta.common.util.Response
import com.github.aakira.napier.Napier
import io.ktor.client.features.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class AddressViewModel(
    private val addressRepository: AddressRepository,
    private val notificationRepo: NotificationRepo,
    private val analTracker: AnalyticsTracker
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

        analTracker.userProp(UserProps.ZIPCODE, address.zipCodeItem.code)
        analTracker.log(AnalTag.SAVE_ADDRESS) {
            param("type", if (addressExists) "updateAddress" else "saveAddress")
            param("zipcode", address.zipCodeItem.code)
        }

        loadSavedAddresses()
    }

    fun loadSavedAddresses() = viewModelScope.launch {
        val addresses = addressRepository.getAddresses()
        Napier.d("loadSavedAddresses(): $addresses")
        analTracker.log(AnalTag.LOAD_SAVED_ADDRESSES) {
            param("address_count", addresses.size.toString())
        }
        addressesViewState.value = ListViewState.Success(addresses)
    }

    fun clearAllAddresses() = viewModelScope.launch {
        addressRepository.removeAddresses()
        analTracker.log(AnalTag.CLEAR_ALL_ADDRESSES)
        loadSavedAddresses()
    }

    fun removeAddress(address: Address) = viewModelScope.launch {
        addressRepository.removeAddress(address)
        analTracker.log(AnalTag.REMOVE_ADDRESS) {
            param("address", address.fullAddress)
        }
        loadSavedAddresses()
    }

    fun searchZipCode(searchQuery: String) = viewModelScope.launch {
        addressRepository.searchZipCodes(searchQuery).collect {
            zipCodeItemsViewState.value = it.toViewState()
            analTracker.log(AnalTag.SEARCH_ZIP_CODE) {
                param("query", searchQuery)
            }
        }
    }

    fun searchStreets(searchQuery: String, zipCodeItem: ZipCodeItem) = viewModelScope.launch {
        addressRepository.searchStreets(searchQuery, zipCodeItem).collect {
            analTracker.log(AnalTag.SEARCH_STREETS) {
                param("query", searchQuery)
                param("zipcode", zipCodeItem.code)
            }
            streetsViewState.value = it.toViewState()
        }
    }

    fun validateAddress(zipCodeItem: ZipCodeItem, street: Street, houseNumber: Int) = viewModelScope.launch {
        addressRepository.validateAddress(zipCodeItem, street, houseNumber).collect {
            validationViewState.value = when (it) {
                is Response.Loading -> ValidationViewState.Loading
                is Response.Success -> {
                    analTracker.log(AnalTag.VALIDATE_ADDRESS){
                        param("zipcode", zipCodeItem.code)
                        param("street", street.names.nl)
                        param("houseNumber", houseNumber.toString())
                    }
                    ValidationViewState.Success(it.body)
                }
                is Response.Error -> {
                    analTracker.log(AnalTag.VALIDATE_ADDRESS) {
                        param("error", it.error?.localizedMessage ?: "")
                    }
                    when (it.error) {
                        is ClientRequestException -> ValidationViewState.InvalidCombination
                        is InvalidAddressException -> ValidationViewState.InvalidAddressSpecified
                        else -> ValidationViewState.NetworkError
                    }
                }
            }
        }
    }

    fun validateExistingAddress(address: Address) = viewModelScope.launch {
        addressRepository.validateExistingAddress(address).collect {
            validationViewState.value = when (it) {
                is Response.Loading -> ValidationViewState.Loading
                is Response.Success -> {
                    analTracker.log(AnalTag.VALIDATE_EXISTING_ADDRESS) {
                        param("address", address.fullAddress)
                    }
                    ValidationViewState.Success(it.body)
                }
                is Response.Error -> {
                    analTracker.log(AnalTag.VALIDATE_EXISTING_ADDRESS) {
                        param("error", it.error?.localizedMessage ?: "")
                    }
                    when (it.error) {
                        is ClientRequestException -> ValidationViewState.InvalidCombination
                        is InvalidAddressException -> ValidationViewState.InvalidAddressSpecified
                        else -> ValidationViewState.NetworkError
                    }
                }
            }
        }
    }

    fun resetAll() {
        validationViewState.value = ValidationViewState.Empty
        zipCodeItemsViewState.value = ListViewState.Empty()
        streetsViewState.value = ListViewState.Empty()
        analTracker.log(AnalTag.RESET_ALL)
    }

    fun resetValidation() {
        validationViewState.value = ValidationViewState.Empty
        analTracker.log(AnalTag.RESET_VALIDATION)
    }

    private fun createDefaultNotificationSettings(address: Address) {
        notificationRepo.insertDefaultNotificationProps(address)
        analTracker.log(AnalTag.CREATE_DEFAULT_NOTIFICATION_SETTINGS)
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