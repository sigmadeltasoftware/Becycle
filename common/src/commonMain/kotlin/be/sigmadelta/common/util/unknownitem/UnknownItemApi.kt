package be.sigmadelta.common.util.unknownitem

import be.sigmadelta.common.util.ApiResponse
import com.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

class UnknownItemApi(private val client: HttpClient) {

    suspend fun logUnknownCollection(item: UnknownCollectionItem) = try {
        val response = client.post<UnknownCollectionItem> {
            url("$BASE_URL/$UNKNOWN_COLLECTION_API")
            contentType(ContentType.Application.Json)
            body = item
        }
        Napier.d("response = $response")
        ApiResponse.Success(response)
    } catch (e: Throwable) {
        ApiResponse.Error(e)
    }

    companion object {
        private const val BASE_URL = "https://sigmadelta-becycle.firebaseio.com"
        private const val UNKNOWN_COLLECTION_API = "unknowncollections.json"
    }
}