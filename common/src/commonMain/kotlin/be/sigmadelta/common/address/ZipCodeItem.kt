package be.sigmadelta.common.address

import be.sigmadelta.common.util.TranslationContainer
import kotlinx.serialization.Serializable

@Serializable
data class ZipCodeItem(
    val id: String,
    val city: City,
    val code: String,
    val names: List<TranslationContainer>
)