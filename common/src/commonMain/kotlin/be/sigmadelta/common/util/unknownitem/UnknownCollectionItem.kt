package be.sigmadelta.common.util.unknownitem

import kotlinx.serialization.Serializable

@Serializable
data class UnknownCollectionItem(
    val fullAddress: String,
    val dateYyyyMmDd: String,
    val collectionData: String
)