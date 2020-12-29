package be.sigmadelta.becycle.common.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AmbientContext
import be.sigmadelta.common.collections.CollectionType

@Composable fun Int.str(): String = AmbientContext.current.getString(this)

fun CollectionType.name(ctx: Context) = ctx.getString(when (this) {
    CollectionType.GFT -> TODO()
    CollectionType.GFT_DIFTAR -> TODO()
    CollectionType.RESIDUAL_HOUSEHOLD_WASTE -> TODO()
    CollectionType.RESIDUAL_HOUSEHOLD_WASTE_DIFTAR -> TODO()
    CollectionType.GROF_HUISVUIL -> TODO()
    CollectionType.GROF_HUISVUIL_APPOINTMENT -> TODO()
    CollectionType.PAPER_CARTON -> TODO()
    CollectionType.PMD -> TODO()
    CollectionType.TEXTILE -> TODO()
    CollectionType.LARGE_HOUSEHOLD_WASTE_APPOINTMENT -> TODO()
    CollectionType.SOFT_PLASTICS -> TODO()
    CollectionType.BATTERIES -> TODO()
    CollectionType.GLASS -> TODO()
    CollectionType.SNOEIHOUT -> TODO()
    CollectionType.SNOEIHOUT_APPOINTMENT -> TODO()
    CollectionType.OLD_METALS -> TODO()
    CollectionType.RE_USE_CENTER -> TODO()
    CollectionType.IJZER -> TODO()
    CollectionType.CHRISTMAS_TREES -> TODO()
    CollectionType.HOUSEHOLD_HAZARDEOUS_WASTE_KGA -> TODO()
    CollectionType.UNKNOWN -> TODO()
})