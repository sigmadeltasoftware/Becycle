package be.sigmadelta.common.address.recapp.legacy

import be.sigmadelta.common.address.RecAppAddressDao
import be.sigmadelta.common.address.recapp.RecAppCityDao
import be.sigmadelta.common.address.recapp.RecAppStreetDao
import be.sigmadelta.common.address.recapp.RecAppStreetZipCodeDao
import be.sigmadelta.common.address.recapp.RecAppZipCodeItemDao
import be.sigmadelta.common.util.TranslationContainer
import kotlinx.serialization.Serializable
import org.kodein.db.model.orm.Metadata
import org.kodein.memory.util.UUID

@Serializable
data class LegacyRecappAddress(
    val zipCodeItem: LegacyRecappZipCodeItem,
    val street: LegacyRecapStreet,
    val houseNumber: Int,
    override val id: String = UUID.randomUUID().toString()
) : Metadata {
    val streetWithHouseNr = "${street.names.nl} $houseNumber"
    val fullAddress = "$streetWithHouseNr | ${zipCodeItem.code} ${zipCodeItem.names.firstOrNull()?.nl}"

    fun toRecappAddressDao() = RecAppAddressDao(
        zipCodeItem = zipCodeItem.toZipCodeItem(),
        street = street.toStreet(),
        houseNumber = houseNumber,
        id = id
    )
}

@Serializable
data class LegacyRecappZipCodeItem(
    val id: String,
    val city: LegacyRecapCity,
    val code: String,
    val names: List<TranslationContainer>
) {
    fun toZipCodeItem() = RecAppZipCodeItemDao(
        id = id,
        city = city.toCity(),
        code = code,
        names = names
    )
}

@Serializable
data class LegacyRecapCity (
    val id: String,
    val name: String,
    val names: TranslationContainer,
    val zipcodes: List<String>,
) {
    fun toCity() = RecAppCityDao(
        id = id,
        name = name,
        names = names,
        zipcodes = zipcodes
    )
}

@Serializable
data class LegacyRecapStreet (
    val id: String,
    val names: TranslationContainer,
    val zipcode: List<LegacyRecapStreetZipCode>,
    val city: List<LegacyRecapCity>,
    val deleted: Boolean
) {
    fun toStreet() = RecAppStreetDao(
        id = id,
        names = names,
        zipcode = zipcode.map { it.toStreetZipCode() },
        city = city.map { it.toCity() },
        deleted = deleted
    )
}

// Street has a very ZipCodeItem looking object, with the exception of the 'city' field
// being a string id instead of a full-fledged object
@Serializable
data class LegacyRecapStreetZipCode(
    val id: String,
    val city: String,
    val code: String,
    val names: List<TranslationContainer>
) {
    fun fromZipCodeItem(zipCodeItem: LegacyRecappZipCodeItem) = LegacyRecapStreetZipCode(
        zipCodeItem.id,
        zipCodeItem.city.id,
        zipCodeItem.code,
        zipCodeItem.names
    )

    fun toStreetZipCode() = RecAppStreetZipCodeDao(
        id = id,
        city = city,
        code = code,
        names = names
    )
}