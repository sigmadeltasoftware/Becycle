package be.sigmadelta.common.util

import android.content.Context
import be.sigmadelta.Becycle.common.R
import be.sigmadelta.common.collections.CollectionType

fun CollectionType.name(ctx: Context) = ctx.getString(when (this) {
    CollectionType.GFT -> R.string.collectiontype__GFT
    CollectionType.GFT_DIFTAR -> R.string.collectiontype__GFT_DIFTAR
    CollectionType.GENERAL_HOUSEHOLD_WASTE -> R.string.collectiontype__RESIDUAL_HOUSEHOLD_WASTE
    CollectionType.GENERAL_HOUSEHOLD_WASTE_DIFTAR -> R.string.collectiontype__RESIDUAL_HOUSEHOLD_WASTE_DIFTAR
    CollectionType.GROF_HUISVUIL -> R.string.collectiontype__GROF_HUISVUIL
    CollectionType.GROF_HUISVUIL_APPOINTMENT -> R.string.collectiontype__GROF_HUISVUIL_APPOINTMENT
    CollectionType.PAPER_CARDBOARD -> R.string.collectiontype__PAPER_CARTON
    CollectionType.PMD -> R.string.collectiontype__PMD
    CollectionType.TEXTILE -> R.string.collectiontype__TEXTILE
    CollectionType.LARGE_HOUSEHOLD_WASTE_APPOINTMENT -> R.string.collectiontype__LARGE_HOUSEHOLD_WASTE_APPOINTMENT
    CollectionType.SOFT_PLASTICS -> R.string.collectiontype__SOFT_PLASTICS
    CollectionType.BATTERIES -> R.string.collectiontype__BATTERIES
    CollectionType.GLASS -> R.string.collectiontype__GLASS
    CollectionType.SNOEIHOUT -> R.string.collectiontype__SNOEIHOUT
    CollectionType.SNOEIHOUT_APPOINTMENT -> R.string.collectiontype__SNOEIHOUT_APPOINTMENT
    CollectionType.OLD_METALS -> R.string.collectiontype__OLD_METALS
    CollectionType.RE_USE_CENTER -> R.string.collectiontype__RE_USE_CENTER
    CollectionType.IJZER -> R.string.collectiontype__IJZER
    CollectionType.CHRISTMAS_TREES -> R.string.collectiontype__CHRISTMAS_TREES
    CollectionType.HOUSEHOLD_HAZARDEOUS_WASTE_KGA -> R.string.collectiontype__HOUSEHOLD_HAZARDEOUS_WASTE_KGA
    CollectionType.UNKNOWN -> R.string.collectiontype__UNKNOWN
})