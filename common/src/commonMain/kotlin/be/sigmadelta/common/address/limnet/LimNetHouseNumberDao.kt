package be.sigmadelta.common.address.limnet

import kotlinx.serialization.Serializable

@Serializable
data class LimNetHouseNumberDao (val huisNummer: String, val toevoeging: String? = "")