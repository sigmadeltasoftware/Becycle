package be.sigmadelta.common.collections

import be.sigmadelta.common.util.TranslationContainer
import kotlinx.serialization.Serializable
import org.kodein.db.model.orm.Metadata
import org.kodein.memory.util.UUID

@Serializable
data class Collection (
    override val id: String = UUID.randomUUID().toString(),
    val timestamp: String,
    val type: String,
    val fraction: CollectionFraction,
    val addressId: String? = null
): Metadata

@Serializable
data class CollectionFraction(
    val id: String,
    val national: Boolean,
    val nationalRef: String?,
    val name: TranslationContainer,
    val color: String,
    val createdAt: String,
    val updatedAt: String,
    val organisation: String
)