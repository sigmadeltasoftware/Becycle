package be.sigmadelta.common.util.unknownitem

import be.sigmadelta.common.util.ApiResponse
import com.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

class UnknownItemApi(
    private val client: HttpClient,
    private val appVersion: String
) {

    suspend fun logUnknownCollection(item: UnknownCollectionItem) = try {
        client.post<UnknownCollectionItem> {
            url("$BASE_URL/$UNKNOWN_COLLECTION_API")
            contentType(ContentType.Application.Json)
            body = UnknownCollectionItemBody.fromUnknownCollectionItem(item, appVersion)
        }

        ApiResponse.Success(item)
    } catch (e: Throwable) {
        ApiResponse.Error(e)
    }

    companion object {
        private const val BASE_URL = "https://sigmadelta-becycle.firebaseio.com"
        private const val UNKNOWN_COLLECTION_API = "unknowncollections.json"
    }
}

@Serializable
data class UnknownCollectionItemBody(
    val fullAddress: String,
    val dateYyyyMmDd: String,
    val collectionData: String,
    val version: String
) {
    companion object {
        fun fromUnknownCollectionItem(item: UnknownCollectionItem, version: String) =
            UnknownCollectionItemBody(
                fullAddress = item.fullAddress,
                dateYyyyMmDd = item.dateYyyyMmDd,
                collectionData = item.collectionData,
                version = version,
            )
    }
}