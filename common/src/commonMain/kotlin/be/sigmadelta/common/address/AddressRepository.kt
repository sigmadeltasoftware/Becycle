package be.sigmadelta.common.address

import be.sigmadelta.common.util.Response
import be.sigmadelta.common.util.apiRequestToFlow
import be.sigmadelta.common.util.apiSearchRequestToFlow
import kotlinx.coroutines.flow.Flow
import org.kodein.db.DB
import org.kodein.db.deleteAll
import org.kodein.db.find
import org.kodein.db.useModels

class AddressRepository(private val db: DB, private val addressApi: AddressApi) {

    fun insertAddress(address: Address) = db.put(address)

    fun getAddresses() = db.find<Address>().all().useModels { it.toList() }

    fun removeAddresses() = db.deleteAll(db.find<Address>().all())

    suspend fun searchZipCodes(queryString: String): Flow<Response<List<ZipCodeItem>>> =
        addressApi.getZipCodes(queryString).apiSearchRequestToFlow()

    suspend fun searchStreets(queryString: String, zipCodeItem: ZipCodeItem) =
        addressApi.getStreets(queryString, zipCodeItem).apiSearchRequestToFlow()

    suspend fun validateAddress(zipCodeItem: ZipCodeItem, street: Street) =
        addressApi.validateAddress(zipCodeItem, street).apiRequestToFlow()
}
