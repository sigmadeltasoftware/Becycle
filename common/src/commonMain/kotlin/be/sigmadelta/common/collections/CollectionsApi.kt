package be.sigmadelta.common.collections

import be.sigmadelta.common.address.Address
import be.sigmadelta.common.util.SearchQueryResult
import be.sigmadelta.common.util.SessionStorage
import be.sigmadelta.common.util.getApi
import io.ktor.client.*
import io.ktor.client.request.*
import kotlin.random.Random

class CollectionsApi(
    private val baseUrl: String,
    private val client: HttpClient,
    private val sessionStorage: SessionStorage
) {

    suspend fun getCollections(
        address: Address,
        fromDateYyyyMmDd: String,
        untilDateYyyyMmDd: String,
        size: Int
    ) = client.getApi<SearchQueryResult<Collection>> {
        url("$baseUrl/$COLLECTIONS_API")
        sessionStorage.attachHeaders(this)
        url.encodedPath = "${url.encodedPath}?zipcodeId=${address.zipCodeItem.id}&streetId=${address.street.id}&fromDate=$fromDateYyyyMmDd&untilDate=$untilDateYyyyMmDd&houseNumber=${address.houseNumber}&size=$size"
        // NOTE: I had to construct the url manually here as Ktor would use character encoding on the streetId parameter, which would cause the API request to fail
        // creating the request post encoding, makes sure that the parameter stays intact
    }

    companion object {
        const val COLLECTIONS_API = "collections"
    }
}

