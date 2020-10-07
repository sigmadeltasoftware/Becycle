package be.sigmadelta.common.address

import kotlinx.serialization.Serializable
import org.kodein.db.model.orm.Metadata

@Serializable
data class Address (override val id: String,
                    val zipCodeItem: ZipCodeItem,
                    val street: Street): Metadata
