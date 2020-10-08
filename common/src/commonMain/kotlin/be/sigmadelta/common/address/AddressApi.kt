package be.sigmadelta.common.address

import be.sigmadelta.common.util.ApiResponse
import be.sigmadelta.common.util.SearchQueryResult
import be.sigmadelta.common.util.SessionStorage
import be.sigmadelta.common.util.getApi
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import org.kodein.memory.util.UUID

class AddressApi(
    private val baseUrl: String,
    private val client: HttpClient,
    private val sessionStorage: SessionStorage
) {

    suspend fun getZipCodes(searchQuery: String) = client.getApi<SearchQueryResult<ZipCodeItem>> {
        url("$baseUrl/$ZIPCODES_API")
        parameter("q", searchQuery)
        sessionStorage.attachHeaders(this)
    }

    suspend fun getStreets(searchQuery: String, zipCodeItem: ZipCodeItem) = client.getApi<SearchQueryResult<Street>> {
        url("$baseUrl/$STREETS_API")
        parameter("q", searchQuery)
        parameter("zipcodes", zipCodeItem.id)
        sessionStorage.attachHeaders(this)
    }

    suspend fun validateAddress(zipCodeItem: ZipCodeItem, street: Street, houseNumber: Int): ApiResponse<Address> = try {
        client.head<Unit> {
            url("$baseUrl/$STREETS_API")
            parameter("zipcodeId", zipCodeItem.id)
            parameter("streetId", street.id)
            sessionStorage.attachHeaders(this)
        }
        ApiResponse.Success(Address(UUID.randomUUID().toString(), zipCodeItem, street, houseNumber))
    } catch (e: Throwable) {
        ApiResponse.Error(e)
    }

    companion object {
        const val ZIPCODES_API = "zipcodes"
        const val STREETS_API = "streets"
    }
}

