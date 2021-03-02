package be.sigmadelta.common.address.recapp

import be.sigmadelta.common.util.TranslationContainer
import kotlinx.serialization.Serializable

@Serializable
data class RecAppStreetDao (
    val id: String,
    val names: TranslationContainer,
    val zipcode: List<RecAppStreetZipCodeDao>,
    val city: List<RecAppCityDao>,
    val deleted: Boolean
)

// Street has a very ZipCodeItem looking object, with the exception of the 'city' field
// being a string id instead of a full-fledged object
@Serializable
data class RecAppStreetZipCodeDao(
    val id: String,
    val city: String,
    val code: String,
    val names: List<TranslationContainer>
) {
    fun fromZipCodeItem(zipCodeItem: RecAppZipCodeItemDao) = RecAppStreetZipCodeDao(
        zipCodeItem.id,
        zipCodeItem.city.id,
        zipCodeItem.code,
        zipCodeItem.names
    )
}