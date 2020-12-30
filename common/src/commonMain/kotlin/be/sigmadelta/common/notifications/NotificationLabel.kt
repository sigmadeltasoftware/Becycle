package be.sigmadelta.common.notifications

import kotlinx.serialization.Serializable
import org.kodein.db.model.orm.Metadata

@Serializable
data class NotificationLabel (override val id: String): Metadata