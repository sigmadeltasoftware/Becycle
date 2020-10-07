package be.sigmadelta.common.util

import io.ktor.client.request.*

class SessionStorage {
    var baseHeaders: List<Header>? = null
    var accessToken: String? = null

    fun getHeaders() = baseHeaders?.toMutableList()?.apply {
        accessToken?.let { add(Header("Authorization", it)) }
    }

    fun attachHeaders(httpRequestBuilder: HttpRequestBuilder) {
        getHeaders()?.forEach {
            httpRequestBuilder.headers.append(it.key, it.value)
        }
    }
}