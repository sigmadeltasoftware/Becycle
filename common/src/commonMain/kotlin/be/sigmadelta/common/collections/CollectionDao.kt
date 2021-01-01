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
        "PMD" -> CollectionType.PMD
        "GFT",
        "GROENAFVAL" -> CollectionType.GFT
        "PAPIER & KARTON" -> CollectionType.PAPER_CARDBOARD
        "HUISVUIL" -> CollectionType.GENERAL_HOUSEHOLD_WASTE
        "KERSTBOOM" -> CollectionType.CHRISTMAS_TREES
        else -> CollectionType.UNKNOWN
    }
}