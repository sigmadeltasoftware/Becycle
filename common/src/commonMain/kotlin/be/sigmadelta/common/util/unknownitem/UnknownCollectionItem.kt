package be.sigmadelta.common.util.unknownitem

import kotlinx.serialization.Serializable
import org.kodein.db.model.orm.Metadata
import org.kodein.memory.util.UUID

@Serializable
data class UnknownCollectionItem(
    val fullAddress: String,
    val dateYyyyMmDd: String,
    val collectionData: String,
    override val id: String = UUID.randomUUID().toString(),
) : Metadata