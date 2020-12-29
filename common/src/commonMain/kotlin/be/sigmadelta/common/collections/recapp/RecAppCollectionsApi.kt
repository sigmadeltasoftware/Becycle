package be.sigmadelta.common.collections.recapp

import be.sigmadelta.common.address.RecAppAddressDao
import be.sigmadelta.common.util.ApiResponse
import be.sigmadelta.common.util.SearchQueryResult
import be.sigmadelta.common.util.SessionStorage
import com.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

class RecAppCollectionsApi(
    private val baseUrl: String,
    private val client: HttpClient,
    private val sessionStorage: SessionStorage
) {

    suspend fun getCollections(
        address: RecAppAddressDao,
        fromDateYyyyMmDd: String,
        untilDateYyyyMmDd: String,
        size: Int
    ): ApiResponse<SearchQueryResult<RecAppCollectionDao>> = try {
        val response = client.get<SearchQueryResult<CollectionResponse>> {
            url("$baseUrl/$COLLECTIONS_API")
            sessionStorage.attachHeaders(this)
            url.encodedPath =
                "${url.encodedPath}?zipcodeId=${address.zipCodeItem.id}&streetId=${address.street.id}&fromDate=$fromDateYyyyMmDd&untilDate=$untilDateYyyyMmDd&houseNumber=${address.houseNumber}&size=$size"
            // NOTE: I had to construct the url manually here as Ktor would use character encoding on the streetId parameter, which would cause the API request to fail
            // creating the request post encoding, makes sure that the parameter stays intact
        }
        Napier.d("response = $response")
        val result = SearchQueryResult(
            response.items.map { it.toCollection(address) },
            response.total,
            response.pages,
            response.size,
            response.self,
            response.first,
            response.last
        )

        ApiResponse.Success(result)
    } catch (e: Throwable) {
        ApiResponse.Error(e)
    }

    companion object {
        const val COLLECTIONS_API = "collections"
    }
}

@Serializable
data class CollectionResponse (
    val timestamp: String,
    val type: String,
    val fraction: RecAppCollectionFractionDao,
) {
    fun toCollection(address: RecAppAddressDao) = RecAppCollectionDao(
        timestamp = this.timestamp,
        fraction = this.fraction,
        type = this.type,
        addressId = address.id
    )
}

