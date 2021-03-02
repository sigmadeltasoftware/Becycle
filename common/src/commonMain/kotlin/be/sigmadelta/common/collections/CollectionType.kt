package be.sigmadelta.common.collections

import kotlinx.serialization.Serializable

@Serializable
enum class CollectionType { // Maps to CollectionFraction.id for RecApp
    GFT,
    GFT_DIFTAR,
    RESIDUAL_HOUSEHOLD_WASTE, // TODO: Remove RESIDUAL_HOUSEHOLD_WASTE(_DIFTAR) once migration phase is over. These are old names for collectiontypes
    GENERAL_HOUSEHOLD_WASTE,
    RESIDUAL_HOUSEHOLD_WASTE_DIFTAR,
    GENERAL_HOUSEHOLD_WASTE_DIFTAR,
    GROF_HUISVUIL,
    GROF_HUISVUIL_APPOINTMENT,
    PAPER_CARTON, // TODO: Remove PAPER_CARTON once migration phase is over. These are old names for collectiontypes
    PAPER_CARDBOARD,
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
    STONE_RUBBLE,
    WOOD,
    UNKNOWN;

    fun isOnAppointment() = this.name.toLowerCase().contains("appointment")
}