package be.sigmadelta.common.address

import be.sigmadelta.common.util.TranslationContainer
import kotlinx.serialization.Serializable

@Serializable
data class City (
    val id: String,
    val name: String,
    val names: TranslationContainer,
    val zipcodes: List<String>,
)