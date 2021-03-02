package be.sigmadelta.becycle.address

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.sigmadelta.becycle.common.analytics.AnalTag
import be.sigmadelta.becycle.common.analytics.AnalyticsTracker
import be.sigmadelta.becycle.common.ui.util.ListViewState
import be.sigmadelta.becycle.common.ui.util.toViewState
import be.sigmadelta.common.Faction
import be.sigmadelta.common.address.*
import be.sigmadelta.common.address.limnet.LimNetHouseNumberDao
import be.sigmadelta.common.address.limnet.LimNetMunicipalityDao
import be.sigmadelta.common.address.limnet.LimNetStreetDao
import be.sigmadelta.common.address.recapp.RecAppStreetDao
import be.sigmadelta.common.address.recapp.RecAppZipCodeItemDao
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
    val recAppZipCodeItemsViewState = MutableStateFlow<ListViewState<RecAppZipCodeItemDao>>(ListViewState.Empty())
    val recAppStreetsViewState = MutableStateFlow<ListViewState<RecAppStreetDao>>(ListViewState.Empty())
    val validationViewState = MutableStateFlow<ValidationViewState>(ValidationViewState.Empty)

    val limNetMunicipalityViewState = MutableStateFlow<ListViewState<LimNetMunicipalityDao>>(ListViewState.Empty())
    val limNetStreetViewState = MutableStateFlow<ListViewState<LimNetStreetDao>>(ListViewState.Empty())
    val limNetHouseNumberViewState = MutableStateFlow<ListViewState<LimNetHouseNumberDao>>(ListViewState.Empty())

    init {
        loadSavedAddresses()
    }

    fun saveAddress(address: AddressDao) = viewModelScope.launch {
        val generic = address.asGeneric()
        val addressExists = addressRepository.getAddresses().map { it.id }.contains(generic.id)

        if (addressExists) {
            addressRepository.updateAddress(address)
        } else {
            addressRepository.insertAddress(address)
            createDefaultNotificationSettings(generic)
        }

        analTracker.log(
            if (generic.faction == Faction.RECAPP) AnalTag.SAVE_ADDRESS.s() else AnalTag.SAVE_ADDRESS_LIMNET.s()) {
            param("type", if (addressExists) "updateAddress" else "saveAddress")
        }

        loadSavedAddresses()
    }

    fun loadSavedAddresses() = viewModelScope.launch {
        val addresses = addressRepository.getAddresses()
        Napier.d("loadSavedAddresses(): $addresses")
        analTracker.log(AnalTag.LOAD_SAVED_ADDRESSES.s()) {
            param("address_count", addresses.size.toString())
        }
        addressesViewState.value = ListViewState.Success(addresses)
    }

    fun clearAllAddresses() = viewModelScope.launch {
//        addressRepository.removeAddresses()
        analTracker.log(AnalTag.CLEAR_ALL_ADDRESSES.s())
        loadSavedAddresses()
    }

    fun removeAddress(address: Address) = viewModelScope.launch {
        addressRepository.removeAddress(address)
        analTracker.log(AnalTag.REMOVE_ADDRESS.s())
        loadSavedAddresses()
    }

    fun searchZipCode(searchQuery: String) = viewModelScope.launch {
        addressRepository.searchRecAppZipCodes(searchQuery).collect {
            recAppZipCodeItemsViewState.value = it.toViewState()
            analTracker.log(AnalTag.SEARCH_ZIP_CODE.s()) {
                param("query", searchQuery)
            }
        }
    }

    fun searchMunicipality(searchQuery: String) = viewModelScope.launch {
        addressRepository.searchLimNetMunicipalities(searchQuery).collect {
            limNetMunicipalityViewState.value = it.toViewState()
            analTracker.log(AnalTag.SEARCH_MUNICIPALITY_LIMNET.s()) {
                param("query", searchQuery)
            }
        }
    }

    fun searchStreets(searchQuery: String, zipCodeItem: RecAppZipCodeItemDao) = viewModelScope.launch {
        addressRepository.searchRecAppStreets(searchQuery, zipCodeItem).collect {
            analTracker.log(AnalTag.SEARCH_STREETS.s()) {
                param("query", searchQuery)
                param("zipcode", zipCodeItem.code)
            }
            recAppStreetsViewState.value = it.toViewState()
        }
    }

    fun searchStreets(searchQuery: String, municipality: LimNetMunicipalityDao) = viewModelScope.launch {
        addressRepository.searchLimNetStreets(searchQuery, municipality).collect {
            analTracker.log(AnalTag.SEARCH_STREETS_LIMNET.s()) {
                param("query", searchQuery)
                param("municipality", municipality.naam)
            }
            limNetStreetViewState.value = it.toViewState()
        }
    }

    fun searchHouseNumbers(searchQuery: String, street: LimNetStreetDao) = viewModelScope.launch {
        addressRepository.searchLimNetHouseNumbers(searchQuery, street).collect {
            analTracker.log(AnalTag.SEARCH_HOUSENUMBERS_LIMNET.s()) {
                param("street", street.naam)
            }
            limNetHouseNumberViewState.value = it.toViewState()
        }
    }

    fun validateAddress(zipCodeItem: RecAppZipCodeItemDao, street: RecAppStreetDao, houseNumber: Int) = viewModelScope.launch {
        if (addressRepository.getAddresses().map { it.fullAddress }.contains(RecAppAddressDao(zipCodeItem, street, houseNumber).asGeneric().fullAddress)){
            validationViewState.value = ValidationViewState.DuplicateAddressEntry
            return@launch
        }

        addressRepository.validateRecAppAddress(zipCodeItem, street, houseNumber).collect {
            validationViewState.value = when (it) {
                is Response.Loading -> ValidationViewState.Loading
                is Response.Success -> {
                    analTracker.log(AnalTag.VALIDATE_ADDRESS.s()){
                        param("zipcode", zipCodeItem.code)
                        param("street", street.names.nl)
                    }
                    ValidationViewState.Success(it.body)
                }
                is Response.Error -> {
                    analTracker.log(AnalTag.VALIDATE_ADDRESS.s()) {
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

    fun validateExistingRecAppAddress(address: RecAppAddressDao) = viewModelScope.launch {
        addressRepository.validateExistingRecAppAddress(address).collect {
            validationViewState.value = when (it) {
                is Response.Loading -> ValidationViewState.Loading
                is Response.Success -> {
                    analTracker.log(AnalTag.VALIDATE_EXISTING_ADDRESS.s()) {}
                    ValidationViewState.Success(it.body)
                }
                is Response.Error -> {
                    analTracker.log(AnalTag.VALIDATE_EXISTING_ADDRESS.s()) {
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

    fun validateLimNetAddress(address: LimNetAddressDao) = viewModelScope.launch {
        analTracker.log(AnalTag.VALIDATE_EXISTING_ADDRESS.s()) {}
        validationViewState.value = ValidationViewState.Success(address)
    }

    fun resetAll() {
        validationViewState.value = ValidationViewState.Empty
        recAppZipCodeItemsViewState.value = ListViewState.Empty()
        recAppStreetsViewState.value = ListViewState.Empty()
        limNetMunicipalityViewState.value = ListViewState.Empty()
        limNetStreetViewState.value = ListViewState.Empty()
        limNetHouseNumberViewState.value = ListViewState.Empty()
        analTracker.log(AnalTag.RESET_ALL.s())
    }

    fun resetValidation() {
        validationViewState.value = ValidationViewState.Empty
        analTracker.log(AnalTag.RESET_VALIDATION.s())
    }

    private fun createDefaultNotificationSettings(address: Address) {
        notificationRepo.insertDefaultNotificationProps(address)
        analTracker.log(AnalTag.CREATE_DEFAULT_NOTIFICATION_SETTINGS.s())
    }
}

sealed class ValidationViewState {
    object Empty: ValidationViewState()
    object Loading: ValidationViewState()
    data class Success(val address: AddressDao): ValidationViewState()
    object InvalidCombination: ValidationViewState()
    object InvalidAddressSpecified: ValidationViewState()
    object NetworkError: ValidationViewState()
    object DuplicateAddressEntry: ValidationViewState()
}