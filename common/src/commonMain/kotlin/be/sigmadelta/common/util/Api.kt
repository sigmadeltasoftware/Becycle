package be.sigmadelta.common.util

import io.ktor.client.*
import io.ktor.client.request.*

suspend inline fun <reified T> HttpClient.getApi(request: HttpRequestBuilder.() -> Unit) = try {
    val response = get<T> { request.invoke(this) }
    println("makeApiRequest() - response = $response")
    ApiResponse.Success(response)
} catch (e: Throwable) {
    ApiResponse.Error(e)
}
