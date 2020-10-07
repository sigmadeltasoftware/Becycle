package be.sigmadelta.common.accesstoken

import be.sigmadelta.common.util.SessionStorage
import be.sigmadelta.common.util.getApi
import io.ktor.client.*
import io.ktor.client.request.*

class AccessTokenApi(
    private val baseUrl: String,
    private val client: HttpClient,
    private val sessionStorage: SessionStorage
) {

    suspend fun getAccessToken() = client.getApi<AccessToken> {
        url("$baseUrl/$ACCESS_TOKEN_API")
        sessionStorage.attachHeaders(this)
    }

    companion object {
        const val ACCESS_TOKEN_API = "access-token"
    }
}

