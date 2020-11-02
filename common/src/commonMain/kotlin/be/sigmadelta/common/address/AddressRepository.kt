package be.sigmadelta.common.address
import be.sigmadelta.common.collections.CollectionsApi
import be.sigmadelta.common.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.*
import org.kodein.db.*
import org.kodein.memory.use

class AddressRepository(private val db: DB, private val addressApi: AddressApi, private val collectionsApi: CollectionsApi) {

    fun insertAddress(address: Address) = db.put(address)

    fun getAddresses() = db.find<Address>().all().useModels { it.toList() }

    fun removeAddresses() = db.deleteAll(db.find<Address>().all())

    fun removeAddress(address: Address) {
        db.find<Address>().byId(address.id).use {
            if (it.isValid()) {
                db.deleteAll(it)
            }
        }
    }

    fun updateAddress(addressId: String, address: Address) {
        db.find<Address>().byId(addressId).use {
            if (it.isValid()) {
                db.deleteAll(it)
                db.put(address)
            }
        }
    }

    suspend fun searchZipCodes(queryString: String): Flow<Response<List<ZipCodeItem>>> =
        addressApi.getZipCodes(queryString).apiSearchRequestToFlow()

    suspend fun searchStreets(queryString: String, zipCodeItem: ZipCodeItem) =
        addressApi.getStreets(queryString, zipCodeItem).apiSearchRequestToFlow()

    suspend fun validateAddress(zipCodeItem: ZipCodeItem, street: Street, houseNumber: Int): Flow<Response<Address>> =
        flow {
            emit(Response.Loading())
            emit(addressApi.validateAddress(zipCodeItem, street, houseNumber).toResponse(collectionsApi))
        }

    suspend fun validateExistingAddress(address: Address): Flow<Response<Address>> =
        flow {
            emit(Response.Loading())
            emit(addressApi.validateExistingAddress(address).toResponse(collectionsApi))
        }
}

private suspend fun ApiResponse<Address>.toResponse(collectionsApi: CollectionsApi) = when (this) {
    is ApiResponse.Success -> {

        val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            .toYyyyMmDd()
        val untilDate = Clock.System.now().plus(DateTimePeriod(months = 1), TimeZone.currentSystemDefault())
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toYyyyMmDd()

        when (val validationResponse = collectionsApi.getCollections(body, date, untilDate, 10)) {
            is ApiResponse.Success -> Response.Success(body)
            is ApiResponse.Error -> Response.Error(validationResponse.error)
        }
    }

    is ApiResponse.Error -> Response.Error(error)
}