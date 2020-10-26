package be.sigmadelta.common.collections

import be.sigmadelta.common.util.TranslationContainer
import kotlinx.serialization.Serializable
import org.kodein.db.indexSet
import org.kodein.db.model.orm.Metadata
import org.kodein.memory.util.UUID

@Serializable
data class Collection (
    override val id: String = UUID.randomUUID().toString(),
    val timestamp: String,
    val type: String,
    val fraction: CollectionFraction,
    val addressId: String
): Metadata {
    val collectionType = fraction.logo.toCollectionType()
    override fun indexes() = indexSet("addressId" to addressId)
}

@Serializable
data class CollectionFraction(
    val id: String,
    val logo: CollectionFractionLogo,
    val national: Boolean,
    val nationalRef: String?,
    val name: TranslationContainer,
    val color: String,
    val createdAt: String,
    val updatedAt: String,
    val organisation: String
)

@Serializable
data class CollectionFractionLogo(val id: String) {
    fun toCollectionType() = when (id) {
        "5d610b86162c063cc0400101" -> CollectionType.BATTERIES
        "5d610b86162c063cc0400110" -> CollectionType.GLASS
        "5d610b86162c063cc0400125" -> CollectionType.PMD
        "5d610b86162c063cc0400123" -> CollectionType.PAPER_CARTON
        "5d610b86162c063cc0400127" -> CollectionType.SNOEIHOUT
        "5d610b86162c063cc0400128" -> CollectionType.SNOEIHOUT_APPOINTMENT
        "5d610b86162c063cc0400108" -> CollectionType.GFT
        "5d610b86162c063cc0400116" -> CollectionType.GROF_HUISVUIL
        "5d610b86162c063cc0400117" -> CollectionType.GROF_HUISVUIL_APPOINTMENT
        "5d610b86162c063cc0400122" -> CollectionType.OLD_METALS
        "5d610b86162c063cc0400129" -> CollectionType.RE_USE_CENTER
        "5d610b86162c063cc0400133",
        "5d610b86162c063cc0400112" -> CollectionType.RESIDUAL_HOUSEHOLD_WASTE
        "5d610b86162c063cc0400105" -> CollectionType.IJZER
        "5d610b86162c063cc0400102" -> CollectionType.CHRISTMAS_TREES
        "5d610b86162c063cc0400114" -> CollectionType.HOUSEHOLD_HAZARDEOUS_WASTE_KGA
        "5d610b86162c063cc0400131" -> CollectionType.TEXTILE
        else -> CollectionType.UNKNOWN
    }
}

@Serializable
enum class CollectionType { // Maps to CollectionFraction.id
    GFT,
    GFT_DIFTAR,
    RESIDUAL_HOUSEHOLD_WASTE,
    RESIDUAL_HOUSEHOLD_WASTE_DIFTAR,
    GROF_HUISVUIL,
    GROF_HUISVUIL_APPOINTMENT,
    PAPER_CARTON,
    PMD,
    TEXTILE,
    LARGE_HOUSEHOLD_WASTE_APPOINTMENT,
    SOFT_PLASTICS,
    BATTERIES,
    GLASS,
    SNOEIHOUT,
    SNOEIHOUT_APPOINTMENT,
    OLD_METALS,
    RE_USE_CENTER,
    IJZER,
    CHRISTMAS_TREES,
    HOUSEHOLD_HAZARDEOUS_WASTE_KGA,
    UNKNOWN;

    fun isOnAppointment() = this.name.toLowerCase().contains("appointment")
}