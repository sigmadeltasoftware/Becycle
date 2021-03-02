package be.sigmadelta.common.address.limnet

import kotlinx.serialization.Serializable

@Serializable
data class LimNetStreetDao(
    val naam: String,
    val nummer: String
)