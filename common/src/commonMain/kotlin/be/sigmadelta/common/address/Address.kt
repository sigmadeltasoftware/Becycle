package be.sigmadelta.common.address

import be.sigmadelta.common.Faction

data class Address(
    val zipCode: String,
    val municipality: String,
    val street: String,
    val houseNumber: String,
    val faction: Faction,
    val id: String
) {
    val streetWithHouseNr = "$street $houseNumber"
    val fullAddress = "$streetWithHouseNr | $zipCode $municipality"
}