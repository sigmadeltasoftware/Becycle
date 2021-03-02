package be.sigmadelta.common.address.limnet

import kotlinx.serialization.Serializable

@Serializable
data class LimNetMunicipalityDao(
    val naam: String,
    val nisCode: String
)