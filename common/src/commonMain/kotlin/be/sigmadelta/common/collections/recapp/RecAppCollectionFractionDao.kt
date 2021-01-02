package be.sigmadelta.common.collections.recapp

import be.sigmadelta.common.collections.CollectionType
import be.sigmadelta.common.util.TranslationContainer
import kotlinx.serialization.Serializable

@Serializable
data class RecAppCollectionFractionDao(
    val id: String,
    val logo: CollectionFractionLogoDao,
    val national: Boolean,
    val nationalRef: String?,
    val name: TranslationContainer,
    val color: String,
    val createdAt: String,
    val updatedAt: String,
    val organisation: String
)

@Serializable
data class CollectionFractionLogoDao(val id: String) {
    fun toCollectionType() = when (id) {
        "5d610b86162c063cc0400101" -> CollectionType.BATTERIES
        "5d610b86162c063cc0400110" -> CollectionType.GLASS
        "5d610b86162c063cc0400126", // TODO: PMD NIGHT COLLECTION
        "5d610b86162c063cc0400125" -> CollectionType.PMD
        "5d610b86162c063cc0400124", // TODO: PAPER-CARDBOARD NIGHT
        "5d610b86162c063cc0400123" -> CollectionType.PAPER_CARDBOARD
        "5d610b86162c063cc0400127" -> CollectionType.SNOEIHOUT
        "5d610b86162c063cc0400128" -> CollectionType.SNOEIHOUT_APPOINTMENT
        "5d610b86162c063cc0400108" -> CollectionType.GFT
        "5d610b86162c063cc0400116" -> CollectionType.GROF_HUISVUIL
        "5d610b86162c063cc0400117" -> CollectionType.GROF_HUISVUIL_APPOINTMENT
        "5d610b86162c063cc0400122" -> CollectionType.OLD_METALS
        "5d610b86162c063cc0400129" -> CollectionType.RE_USE_CENTER
        "5d610b86162c063cc0400133",
        "5d610b86162c063cc0400112" -> CollectionType.GENERAL_HOUSEHOLD_WASTE
        "5d610b86162c063cc0400105" -> CollectionType.IJZER
        "5d610b86162c063cc0400102" -> CollectionType.CHRISTMAS_TREES
        "5d610b86162c063cc0400114", // TODO: KGA Mobiel
        "5d610b86162c063cc0400115" -> CollectionType.HOUSEHOLD_HAZARDEOUS_WASTE_KGA
        "5d610b86162c063cc0400131" -> CollectionType.TEXTILE
        else -> CollectionType.UNKNOWN
    }
}

