package be.sigmadelta.common.address

import kotlinx.serialization.Serializable
import org.kodein.db.model.orm.Metadata
import org.kodein.memory.util.UUID

@Serializable
data class Address(
    val zipCodeItem: ZipCodeItem,
    val street: Street,
    val houseNumber: Int,
    override val id: String = UUID.randomUUID().toString()
) : Metadata
