package be.sigmadelta.common.collections

import be.sigmadelta.common.Faction
import be.sigmadelta.common.collections.recapp.RecAppCollectionExceptionDao
import be.sigmadelta.common.collections.recapp.RecAppCollectionFractionDao
import be.sigmadelta.common.util.parseYyyyMmDdToLocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import org.kodein.db.indexSet
import org.kodein.db.model.orm.Metadata
import org.kodein.memory.util.UUID

sealed class CollectionDao {
    abstract fun asGeneric(): Collection
}

@Serializable
data class RecAppCollectionDao (
    override val id: String = UUID.randomUUID().toString(),
    val timestamp: String,
    val type: String,
    val fraction: RecAppCollectionFractionDao,
    val addressId: String,
    val exception: RecAppCollectionExceptionDao? = null
): CollectionDao(), Metadata {

    fun collectionType() = fraction.logo.toCollectionType()
    override fun indexes() = indexSet("addressId" to addressId)

    override fun asGeneric() = Collection(
        id = id,
        type = collectionType(),
        date = timestamp.toInstant().toLocalDateTime(TimeZone.currentSystemDefault()),
        faction = Faction.RECAPP,
        exception = exception?.toCollectionException()
    )
}

@Serializable
data class LimNetCollectionDao(
    override val id: String = UUID.randomUUID().toString(),
    val category: String,
    val date: String,
    val addressId: String,
    val description: String?,
    val detailUrl: String?,
    val location: String?,
): CollectionDao(), Metadata {
    fun collectionType() = toCollectionType()
    override fun indexes() = indexSet("addressId" to addressId)

    override fun asGeneric() = Collection(
        id = id,
        type = collectionType(),
        date = date.parseYyyyMmDdToLocalDateTime(),
        faction = Faction.LIMNET,
        exception = null
    )

    private fun toCollectionType(): CollectionType = when(category.trim().toUpperCase()) {
        //TODO Look into limburgnet and get the other types of possible events and add them here
        "GFT",
        "GROENAFVAL" -> CollectionType.GFT
        "GROFVUIL" -> CollectionType.GROF_HUISVUIL
        "HUISVUIL" -> CollectionType.GENERAL_HOUSEHOLD_WASTE
        "KERSTBOOM" -> CollectionType.CHRISTMAS_TREES
        "METAAL" -> CollectionType.OLD_METALS
        "PAPIER & KARTON" -> CollectionType.PAPER_CARDBOARD
        "PLASTICS" -> CollectionType.SOFT_PLASTICS
        "PMD" -> CollectionType.PMD
        "TEXTIEL" -> CollectionType.TEXTILE
        else -> CollectionType.UNKNOWN
    }
}