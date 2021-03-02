package be.sigmadelta.common.address.recapp

import be.sigmadelta.common.util.TranslationContainer
import kotlinx.serialization.Serializable

@Serializable
data class RecAppCityDao (
    val id: String,
    val name: String,
    val names: TranslationContainer,
    val zipcodes: List<String>,
)