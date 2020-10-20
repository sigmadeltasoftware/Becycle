package be.sigmadelta.becycle.common.ui.util

import be.sigmadelta.common.util.Response

sealed class ListViewState<T> {
    class Empty<T> : ListViewState<T>()
    class Loading<T> : ListViewState<T>()
    data class Success<T>(val payload: List<T>) : ListViewState<T>()
    data class Error<T>(val error: Throwable?) : ListViewState<T>()
}

sealed class ViewState<T> {
    class Empty<T> : ViewState<T>()
    class Loading<T> : ViewState<T>()
    data class Success<T>(val payload: T) : ViewState<T>()
    data class Error<T>(val error: Throwable?) : ViewState<T>()
}

fun <V> Response<List<V>>.toViewState() = when (this) {
    is Response.Loading -> ListViewState.Loading()
    is Response.Success -> if (this.body.isEmpty())
        ListViewState.Empty()
    else
        ListViewState.Success(body)
    is Response.Error -> ListViewState.Error(error)
}

fun <V> Response<V>.toViewState() = when (this) {
    is Response.Loading -> ViewState.Loading()
    is Response.Success -> ViewState.Success(body)
    is Response.Error -> ViewState.Error(error)
}