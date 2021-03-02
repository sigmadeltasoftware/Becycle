package be.sigmadelta.common.accesstoken

import kotlinx.serialization.Serializable

@Serializable
data class AccessToken(val accessToken: String)