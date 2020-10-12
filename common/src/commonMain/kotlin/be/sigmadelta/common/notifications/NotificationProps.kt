package be.sigmadelta.common.notifications

import be.sigmadelta.common.collections.CollectionType
import be.sigmadelta.common.util.TranslationContainer
import kotlinx.serialization.Serializable
import org.kodein.db.model.orm.Metadata
import org.kodein.memory.util.UUID

@Serializable
data class NotificationProps (
    override val id: String = UUID.randomUUID().toString(),
    val addressId: String,
    val zipCode: String,
    val streetName: TranslationContainer,
    val collectionSettings: List<CollectionNotificationProps>
): Metadata

@Serializable
data class CollectionNotificationProps(
    val type: CollectionType?,
    val enabled: Boolean
)