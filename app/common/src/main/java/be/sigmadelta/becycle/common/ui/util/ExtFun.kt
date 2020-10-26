package be.sigmadelta.becycle.common.ui.util

import be.sigmadelta.becycle.common.R
import be.sigmadelta.common.collections.CollectionType

fun CollectionType.iconRef() = when (this) {
    CollectionType.GFT -> R.drawable.ic_gft
    CollectionType.GFT_DIFTAR -> R.drawable.ic_gft
    CollectionType.RESIDUAL_HOUSEHOLD_WASTE -> R.drawable.ic_general_household_waste
    CollectionType.RESIDUAL_HOUSEHOLD_WASTE_DIFTAR -> R.drawable.ic_general_household_waste
    CollectionType.GROF_HUISVUIL -> R.drawable.ic_grof_huisvuil
    CollectionType.GROF_HUISVUIL_APPOINTMENT -> R.drawable.ic_grof_huisvuil
    CollectionType.PAPER_CARTON -> R.drawable.ic_paper_cardboard
    CollectionType.PMD -> R.drawable.ic_pmd
    CollectionType.TEXTILE -> R.drawable.ic_textiles
    CollectionType.LARGE_HOUSEHOLD_WASTE_APPOINTMENT -> R.drawable.ic_general_household_waste
    CollectionType.SOFT_PLASTICS -> R.drawable.ic_soft_plastic
    CollectionType.BATTERIES -> R.drawable.ic_batteries
    CollectionType.GLASS -> R.drawable.ic_glass
    CollectionType.SNOEIHOUT -> R.drawable.ic_prunings
    CollectionType.SNOEIHOUT_APPOINTMENT -> R.drawable.ic_prunings
    CollectionType.OLD_METALS -> R.drawable.ic_old_metal
    CollectionType.RE_USE_CENTER -> R.drawable.ic_reuse_center
    CollectionType.IJZER -> R.drawable.ic_old_metal
    CollectionType.CHRISTMAS_TREES -> R.drawable.ic_christmas_trees
    CollectionType.HOUSEHOLD_HAZARDEOUS_WASTE_KGA -> R.drawable.ic_kga
    CollectionType.UNKNOWN -> R.drawable.ic_unknown
}