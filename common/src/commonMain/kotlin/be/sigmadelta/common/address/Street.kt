package be.sigmadelta.common.address

import be.sigmadelta.common.util.TranslationContainer
import kotlinx.serialization.Serializable

@Serializable
data class Street (
    val id: String,
    val names: TranslationContainer,
    val zipcode: List<StreetZipCode>,
    val city: List<City>,
    val deleted: Boolean
)

// Street has a very ZipCodeItem looking object, with the exception of the 'city' field
// being a string id instead of a full-fledged object
@Serializable
data class StreetZipCode(
    val id: String,
    val city: String,
    val code: String,
    val names: List<TranslationContainer>
) {
    fun fromZipCodeItem(zipCodeItem: ZipCodeItem) = StreetZipCode(
        zipCodeItem.id,
        zipCodeItem.city.id,
        zipCodeItem.code,
        zipCodeItem.names
    )
}