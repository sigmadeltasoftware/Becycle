package be.sigmadelta.common.collections

import kotlinx.serialization.Serializable

@Serializable
data class CollectionOverview (
    val today: List<Collection>? = null,
    val tomorrow: List<Collection>? = null,
    val upcoming: List<Collection>? = null
)