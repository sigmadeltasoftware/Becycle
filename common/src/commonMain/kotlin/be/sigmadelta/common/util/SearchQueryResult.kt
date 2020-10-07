package be.sigmadelta.common.util

import kotlinx.serialization.Serializable

@Serializable
data class SearchQueryResult<T> (
    val items: List<T>,
    val total: Int,
    val pages: Int,
    val size: Int,
    val self: String,
    val first: String,
    val last: String
)