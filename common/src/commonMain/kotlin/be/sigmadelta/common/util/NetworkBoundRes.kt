package be.sigmadelta.common.util

import kotlinx.coroutines.flow.*

inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> ResultType,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline onFetchFailed: (Throwable) -> Unit = { Unit },
    crossinline shouldFetch: (ResultType) -> Boolean = { true }
) = flow<Response<ResultType>> {
    emit(Response.Loading())
    val data = query()

    val flow = if (shouldFetch(data)) {
        emit(Response.Loading())

        try {
            saveFetchResult(fetch())
            Response.Success(query())
        } catch (throwable: Throwable) {
            onFetchFailed(throwable)
            Response.Error(throwable)
        }
    } else {
        Response.Success(query())
    }

    emit(flow)
}