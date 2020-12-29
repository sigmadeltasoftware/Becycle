package be.sigmadelta.common.collections

import be.sigmadelta.common.Faction
import kotlinx.datetime.LocalDateTime

data class Collection(
    val id: String,
    val type: CollectionType,
    val date: LocalDateTime,
    val faction: Faction,
)