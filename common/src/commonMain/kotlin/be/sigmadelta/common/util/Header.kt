package be.sigmadelta.common.util

import kotlinx.serialization.Serializable

@Serializable
data class Header(val key: String, val value: String)