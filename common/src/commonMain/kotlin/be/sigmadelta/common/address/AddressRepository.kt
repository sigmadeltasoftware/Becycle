package be.sigmadelta.common.address
import be.sigmadelta.common.Faction
import be.sigmadelta.common.address.recapp.*
import be.sigmadelta.common.collections.recapp.RecAppCollectionsApi
import be.sigmadelta.common.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.*
import org.kodein.db.*
import org.kodein.memory.use

class AddressRepository(
    private val recAppDb: DB,
    private val recAppAddressApi: RecAppAddressApi,
    private val recAppCollectionsApi: RecAppCollectionsApi,
) {

    fun insertAddress(address: AddressDao) = when(address) {
        is RecAppAddressDao -> recAppDb.put(address)
    }

    // TODO: Merge lists and map to generic address type
    fun getAddresses(): List<Address> = recAppDb.find<RecAppAddressDao>().all()
        .useModels { it.toList() }
        .map { it.asGeneric() }

    fun removeAddresses() = recAppDb.deleteAll(recAppDb.find<RecAppAddressDao>().all())

    fun removeAddress(address: Address) = when (address.faction) {
        Faction.RECAPP -> {
            recAppDb.find<RecAppAddressDao>().byId(address.id).use {
                if (it.isValid()) {
                    recAppDb.deleteAll(it)
                }
            }
        }
        Faction.LIMNET -> Unit // TODO
    }

    fun updateAddress(addressId: String, address: AddressDao) = when (address) {
        is RecAppAddressDao -> {
            recAppDb.find<RecAppAddressDao>().byId(addressId).use {
                if (it.isValid()) {
                    recAppDb.deleteAll(it)
                    recAppDb.put(address)
                }
            }
        }
    }

    suspend fun searchRecAppZipCodes(queryString: String): Flow<Response<List<RecAppZipCodeItemDao>>> =
        recAppAddressApi.getZipCodes(queryString).apiSearchRequestToFlow()

    suspend fun searchRecAppStreets(queryString: String, zipCodeItem: RecAppZipCodeItemDao) =
        recAppAddressApi.getStreets(queryString, zipCodeItem).apiSearchRequestToFlow()

    suspend fun validateRecAppAddress(zipCodeItem: RecAppZipCodeItemDao, street: RecAppStreetDao, houseNumber: Int): Flow<Response<RecAppAddressDao>> =
        flow {
            emit(Response.Loading())
            emit(recAppAddressApi.validateAddress(zipCodeItem, street, houseNumber).toResponse(recAppCollectionsApi))
        }

    suspend fun validateExistingRecAppAddress(address: RecAppAddressDao): Flow<Response<RecAppAddressDao>> =
        flow {
            emit(Response.Loading())
            emit(recAppAddressApi.validateExistingAddress(address).toResponse(recAppCollectionsApi))
        }
}

private suspend fun ApiResponse<RecAppAddressDao>.toResponse(collectionsApi: RecAppCollectionsApi) = when (this) {
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