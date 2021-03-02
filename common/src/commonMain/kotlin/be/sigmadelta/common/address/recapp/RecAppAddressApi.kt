package be.sigmadelta.common.address.recapp

import be.sigmadelta.common.address.RecAppAddressDao
import be.sigmadelta.common.util.ApiResponse
import be.sigmadelta.common.util.SearchQueryResult
import be.sigmadelta.common.util.SessionStorage
import be.sigmadelta.common.util.getApi
import io.ktor.client.*
import io.ktor.client.request.*

class RecAppAddressApi(
    private val baseUrl: String,
    private val client: HttpClient,
    private val sessionStorage: SessionStorage
) {

    suspend fun getZipCodes(searchQuery: String) = client.getApi<SearchQueryResult<RecAppZipCodeItemDao>> {
        url("$baseUrl/$ZIPCODES_API")
        parameter("q", searchQuery)
        sessionStorage.attachHeaders(this)
    }

    suspend fun getStreets(searchQuery: String, zipCodeItem: RecAppZipCodeItemDao) = client.getApi<SearchQueryResult<RecAppStreetDao>> {
        url("$baseUrl/$STREETS_API")
        parameter("q", searchQuery)
        parameter("zipcodes", zipCodeItem.id)
        sessionStorage.attachHeaders(this)
    }

    suspend fun validateAddress(zipCodeItem: RecAppZipCodeItemDao, street: RecAppStreetDao, houseNumber: Int): ApiResponse<RecAppAddressDao> = try {
        client.head<Unit> {
            url("$baseUrl/$STREETS_API")
            parameter("zipcodeId", zipCodeItem.id)
            parameter("streetId", street.id)
            sessionStorage.attachHeaders(this)
        }
        ApiResponse.Success(RecAppAddressDao(zipCodeItem, street, houseNumber))
    } catch (e: Throwable) {
        ApiResponse.Error(e)
    }

    suspend fun validateExistingAddress(address: RecAppAddressDao): ApiResponse<RecAppAddressDao> = try {
        client.head<Unit> {
            url("$baseUrl/$STREETS_API")
            parameter("zipcodeId", address.zipCodeItem.id)
            parameter("streetId", address.street.id)
            sessionStorage.attachHeaders(this)
        }
        ApiResponse.Success(address)
    } catch (e: Throwable) {
        ApiResponse.Error(e)
    }

    companion object {
        const val ZIPCODES_API = "zipcodes"
        const val STREETS_API = "streets"
    }
}

