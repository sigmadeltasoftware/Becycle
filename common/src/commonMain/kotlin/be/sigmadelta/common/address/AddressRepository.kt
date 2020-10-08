package be.sigmadelta.common.address

import be.sigmadelta.common.collections.CollectionsApi
import be.sigmadelta.common.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.kodein.db.DB
import org.kodein.db.deleteAll
import org.kodein.db.find
import org.kodein.db.useModels

class AddressRepository(private val db: DB, private val addressApi: AddressApi, private val collectionsApi: CollectionsApi) {

    fun insertAddress(address: Address) = db.put(address)

    fun getAddresses() = db.find<Address>().all().useModels { it.toList() }

    fun removeAddresses() = db.deleteAll(db.find<Address>().all())

    suspend fun searchZipCodes(queryString: String): Flow<Response<List<ZipCodeItem>>> =
        addressApi.getZipCodes(queryString).apiSearchRequestToFlow()

    suspend fun searchStreets(queryString: String, zipCodeItem: ZipCodeItem) =
        addressApi.getStreets(queryString, zipCodeItem).apiSearchRequestToFlow()

    suspend fun validateAddress(zipCodeItem: ZipCodeItem, street: Street, houseNumber: Int): Flow<Response<Address>> =
        flow {
            emit(Response.Loading())

            emit(
                when (val response = addressApi.validateAddress(zipCodeItem, street, houseNumber)) {
                    is ApiResponse.Success -> {
                        val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                        val untilMonth = if (date.monthNumber == 12) 1 else date.monthNumber + 1
                        val untilYear = if (date.monthNumber == 12) date.year + 1 else date.year
                        val untilDate = LocalDateTime(
                            untilYear,
                            untilMonth,
                            date.dayOfMonth,
                            0,
                            0,
                            0,
                            0
                        ).toYyyyMmDd()

                        when (val validationResponse = collectionsApi.getCollections(response.body, date.toYyyyMmDd(), untilDate, 100)) {
                            is ApiResponse.Success -> Response.Success(response.body)
                            is ApiResponse.Error -> Response.Error(validationResponse.error)
                        }
                    }

                    is ApiResponse.Error -> Response.Error(response.error)
                }
            )
        }
}
