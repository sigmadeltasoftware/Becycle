package be.sigmadelta.common.baseheader

import be.sigmadelta.common.util.getApi
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

class RecAppBaseHeadersApi(private val client: HttpClient) {

    suspend fun getBaseHeaders() = client.getApi<BaseHeadersResponse> {
        url("$HEADERS_BASE_URL/$BASE_HEADER_API")
    }

    companion object {
        const val HEADERS_BASE_URL = "https://sigmadelta-becycle.firebaseio.com"
        const val BASE_HEADER_API = "secret.json"
    }
}

@Serializable
data class BaseHeadersResponse(val secret: String, val consumer: String)
