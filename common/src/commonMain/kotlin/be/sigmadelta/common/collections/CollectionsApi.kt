package be.sigmadelta.common.collections

import be.sigmadelta.common.address.Address
import be.sigmadelta.common.util.SearchQueryResult
import be.sigmadelta.common.util.SessionStorage
import be.sigmadelta.common.util.addLeadingZeroBelow10
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
        currentMonth: Int
    ) = client.getApi<SearchQueryResult<Collection>> {
        url("$baseUrl/$COLLECTIONS_API")
        val houseNumber = Random.nextInt(1, 200) // TODO: Housenumber seems to be irrelevant in Recycle API
        sessionStorage.attachHeaders(this)
        val currentMonth = addLeadingZeroBelow10(currentMonth)
        url.encodedPath = "${url.encodedPath}?zipcodeId=${address.zipCodeItem.id}&streetId=${address.street.id}&houseNumber=$houseNumber&fromDate=2020-${currentMonth}-01&untilDate=2020-${currentMonth}-31&size=100"
        // NOTE: I had to construct the url manually here as Ktor would use character encoding on the streetId parameter, which would cause the API request to fail
        // creating the request post encoding, makes sure that the parameter stays intact
    }

    companion object {
        const val COLLECTIONS_API = "collections"
    }
}

