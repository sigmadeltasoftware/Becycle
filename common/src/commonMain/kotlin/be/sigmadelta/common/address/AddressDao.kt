package be.sigmadelta.common.address

import be.sigmadelta.common.Faction
import be.sigmadelta.common.address.recapp.RecAppStreetDao
import be.sigmadelta.common.address.recapp.RecAppZipCodeItemDao
import kotlinx.serialization.Serializable
import org.kodein.db.model.orm.Metadata
import org.kodein.memory.util.UUID

sealed class AddressDao {
    abstract fun asGeneric(): Address
}

@Serializable
data class RecAppAddressDao(
    val zipCodeItem: RecAppZipCodeItemDao,
    val street: RecAppStreetDao,
    val houseNumber: Int,
    override val id: String = UUID.randomUUID().toString()
) : AddressDao(), Metadata {

    override fun asGeneric(): Address = Address(
        zipCode = zipCodeItem.code,
        municipality = zipCodeItem.names.firstOrNull()?.nl ?: "",
        street = street.names.nl,
        houseNumber = houseNumber.toString(),
        faction = Faction.RECAPP,
        id = id
    )
}

