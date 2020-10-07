package be.sigmadelta.common.util

sealed class Response<T> {
    class Loading<T>: Response<T>()
    data class Success<T>(val body: T) : Response<T>()
    data class Error<T>(val error: Throwable? = null) : Response<T>()
}