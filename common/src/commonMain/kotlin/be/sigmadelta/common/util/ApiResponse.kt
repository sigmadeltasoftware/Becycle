package be.sigmadelta.common.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.Serializable

sealed class ApiResponse<T> {
    @Serializable data class Success<T>(val body: T) : ApiResponse<T>()
    data class Error<T>(val error: Throwable? = null) : ApiResponse<T>()
}

fun <T> ApiResponse<SearchQueryResult<T>>.apiSearchRequestToFlow() = flow {
    emit(Response.Loading())
    emit(when (val response = this@apiSearchRequestToFlow) {
        is ApiResponse.Success -> Response.Success(response.body.items)
        is ApiResponse.Error -> Response.Error(response.error)
    })
}.flowOn(Dispatchers.Default).debounce(400L)

fun <T> ApiResponse<T>.apiRequestToFlow() = flow {

    emit(Response.Loading())
    emit(when (val response = this@apiRequestToFlow) {
        is ApiResponse.Success -> Response.Success(response.body)
        is ApiResponse.Error -> Response.Error(response.error)
    })
}.flowOn(Dispatchers.Default).debounce(400L)