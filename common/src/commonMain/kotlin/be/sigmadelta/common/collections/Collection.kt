package be.sigmadelta.common.collections

import be.sigmadelta.common.util.TranslationContainer
import kotlinx.serialization.Serializable
import org.kodein.db.model.orm.Metadata
import org.kodein.memory.util.UUID

@Serializable
data class  Collection (
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

@Serializable
enum class CollectionType(val id: String) { // Maps to CollectionFraction.id
    GFT("5d934933baaaa801627e4106"),
    RESIDUAL_WASTE("5d610b86162c063cc0400112"),
    PAPER_CARTON("5d610b87173c063cc0400102"),
    PMD("5d610b87173c063cc0400103"),
    TEXTILE("5ec7a05e7125b480d73d8598"),
    LARGE_HOUSEHOLD_WASTE_APPOINTMENT("5eb96d15666f0643df6f5236")
}