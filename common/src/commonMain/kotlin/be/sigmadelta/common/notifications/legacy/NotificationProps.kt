package be.sigmadelta.common.notifications.legacy

import be.sigmadelta.common.date.Time
import be.sigmadelta.common.notifications.CollectionNotificationProps
import be.sigmadelta.common.notifications.NotifProps
import be.sigmadelta.common.util.TranslationContainer
import kotlinx.serialization.Serializable
import org.kodein.db.indexSet
import org.kodein.db.model.orm.Metadata
import org.kodein.memory.util.UUID

@Serializable
data class NotificationProps (
    override val id: String = UUID.randomUUID().toString(),
    val addressId: String,
    val zipCode: String,
    val streetName: TranslationContainer,
    val collectionSettings: List<CollectionNotificationProps>,
    val genericTodayAlarmTime: Time,
    val genericTomorrowAlarmTime: Time
): Metadata {
    override fun indexes() = indexSet("addressId" to addressId)

    fun toNotificationProps() = NotifProps(
        id = id,
        addressId = addressId,
        zipCode = zipCode,
        streetName =  streetName.nl,
        collectionSettings = collectionSettings,
        genericTodayAlarmTime = genericTodayAlarmTime,
        genericTomorrowAlarmTime = genericTomorrowAlarmTime
    )
}