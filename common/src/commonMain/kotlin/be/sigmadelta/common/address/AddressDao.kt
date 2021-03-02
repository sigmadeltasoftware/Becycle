package be.sigmadelta.common.address

import be.sigmadelta.common.Faction
import be.sigmadelta.common.address.limnet.LimNetHouseNumberDao
import be.sigmadelta.common.address.limnet.LimNetMunicipalityDao
import be.sigmadelta.common.address.limnet.LimNetNisToZipcodeUtil
import be.sigmadelta.common.address.limnet.LimNetStreetDao
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

@Serializable
data class LimNetAddressDao(
    val municipality: LimNetMunicipalityDao,
    val street: LimNetStreetDao,
    val houseNumber: LimNetHouseNumberDao,
    override val id: String = UUID.randomUUID().toString()
) : AddressDao(), Metadata {

    override fun asGeneric(): Address = Address(
        zipCode = LimNetNisToZipcodeUtil.findZipcodeByNis(municipality.nisCode),
        municipality = municipality.naam,
        street = street.naam,
        houseNumber = houseNumber.huisNummer,
        faction = Faction.LIMNET,
        id = id
    )
}