package be.sigmadelta.common.collections

import kotlinx.serialization.Serializable

@Serializable
enum class CollectionType { // Maps to CollectionFraction.id for RecApp
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