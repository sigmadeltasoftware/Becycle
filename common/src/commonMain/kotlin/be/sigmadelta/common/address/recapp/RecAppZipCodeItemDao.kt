package be.sigmadelta.common.address.recapp

import be.sigmadelta.common.util.TranslationContainer
import kotlinx.serialization.Serializable

@Serializable
data class RecAppZipCodeItemDao(
    val id: String,
    val city: RecAppCityDao,
    val code: String,
    val names: List<TranslationContainer>
)