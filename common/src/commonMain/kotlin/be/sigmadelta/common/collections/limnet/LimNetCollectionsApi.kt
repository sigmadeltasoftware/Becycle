package be.sigmadelta.common.collections.limnet

import be.sigmadelta.common.address.LimNetAddressDao
import be.sigmadelta.common.collections.LimNetCollectionDao
import be.sigmadelta.common.util.ApiResponse
import com.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

class LimNetCollectionsApi(
    private val baseUrl: String,
    private val client: HttpClient
) {

    suspend fun getCollections(
        address: LimNetAddressDao,
        YyyyMm: String,
        YyyyMmNextMonth: String,
        ): ApiResponse<List<LimNetCollectionDao>> = try {
        val response = client.get<LimNetCollectionResponse> {
            url("$baseUrl/${COLLECTIONS_API(address.municipality.nisCode, YyyyMm)}")
            parameter("straatNummer", address.street.nummer)
            parameter("huisNummer", address.houseNumber.huisNummer)
        }
        val responseNextMonth = client.get<LimNetCollectionResponse> {
            url("$baseUrl/${COLLECTIONS_API(address.municipality.nisCode, YyyyMmNextMonth)}")
            parameter("straatNummer", address.street.nummer)
            parameter("huisNummer", address.houseNumber.huisNummer)
        }

        val collections = response.events.map { it.toCollectionDao(address) }.toMutableList()
        collections.addAll(responseNextMonth.events.map { it.toCollectionDao(address) })


        Napier.v("response = $response")

        ApiResponse.Success(collections)
    } catch (e: Throwable) {
        Napier.e("${e.message}\n${e.stackTraceToString()}")
        ApiResponse.Error(e)
    }

    companion object {
        private fun COLLECTIONS_API(nisCode: String, yyyyMm: String) = "kalender/$nisCode/$yyyyMm"
    }
}

@Serializable
data class LimNetCollectionEvent(
    val category: String,
    val date: String,
    val description: String?,
    val detailUrl: String?,
    val location: String?,
) {
    fun toCollectionDao(address: LimNetAddressDao) = LimNetCollectionDao(
        addressId = address.id,
        category = category,
        date = date,
        description = description,
        detailUrl = detailUrl,
        location = location
    )
}

// TODO: Look into extending support as the response is much more complex than just this
@Serializable
data class LimNetCollectionResponse(
    val events: List<LimNetCollectionEvent>
)
