package be.sigmadelta.common.util

import be.sigmadelta.common.Faction
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.address.AddressDao
import be.sigmadelta.common.address.LimNetAddressDao
import be.sigmadelta.common.address.RecAppAddressDao
import be.sigmadelta.common.collections.CollectionDao
import be.sigmadelta.common.collections.CollectionType
import be.sigmadelta.common.collections.LimNetCollectionDao
import be.sigmadelta.common.collections.RecAppCollectionDao
import be.sigmadelta.common.address.recapp.legacy.Address as LegacyAddress
import be.sigmadelta.common.collections.Collection as TrashCollection
import be.sigmadelta.common.db.getApplicationFilesDirectoryPath
import be.sigmadelta.common.notifications.NotificationLabel
import be.sigmadelta.common.notifications.legacy.NotificationProps
import be.sigmadelta.common.util.unknownitem.UnknownCollectionItem
import be.sigmadelta.common.util.unknownitem.UnknownItemRepository
import com.github.aakira.napier.Napier
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.kodein.db.*
import org.kodein.db.impl.factory
import org.kodein.memory.use

class DBManager(private val unknownItemRepository: UnknownItemRepository) {

    inline fun <reified T : Any> findAll(faction: Faction): List<T> = getDb(faction).find<T>()
        .all().use {
            it.useModels { it.toList() }
        }

    fun storeAddress(address: AddressDao) = when (address) {
        is RecAppAddressDao -> recAppDb
        is LimNetAddressDao -> limNetDb
    }.put(address)

    fun removeAddress(address: Address) {
        when (address.faction) {
            Faction.RECAPP -> innerRemoveAddress<RecAppAddressDao>(address.faction, address.id)
            Faction.LIMNET -> innerRemoveAddress<LimNetAddressDao>(address.faction, address.id)
        }
    }

    private inline fun <reified T : Any> innerRemoveAddress(faction: Faction, addressId: String) {
        val db = getDb(faction)
        db.find<T>().byId(addressId).use {
            if (it.isValid()) {
                db.delete(it.key())
            }
        }
    }

    fun updateAddress(address: AddressDao) {
        val db = getDb(address.asGeneric().faction)

        when (address) {
            is RecAppAddressDao -> db.find<RecAppAddressDao>().byId(address.id).use {
                    if (it.isValid()) {
                        db.deleteAll(it)
                        db.put(address)
                    }
                }
            is LimNetAddressDao -> db.find<LimNetAddressDao>().byId(address.id).use {
                if (it.isValid()) {
                    db.deleteAll(it)
                    db.put(address)
                }
            }
        }
    }

    fun findAllAddresses(): List<Address> {
        val addresses = mutableListOf<Address>()
        Faction.values().forEach {
            when (it) {
                Faction.LIMNET -> addresses.addAll(findAll<LimNetAddressDao>(it).map { it.asGeneric() })
                Faction.RECAPP -> addresses.addAll(findAll<RecAppAddressDao>(it).map { it.asGeneric() })
            }
        }
        return addresses
    }

    fun findAllCollectionsByAddress(address: Address): List<TrashCollection> = when (address.faction) {
        Faction.LIMNET -> innerFindAllCollectionsByAddress<LimNetCollectionDao>(address).map {
            it.asGeneric().apply {
                if (type == CollectionType.UNKNOWN) {
                    postAndSaveUnknownCollectionItem(address, it)
                }
            }
        }
        Faction.RECAPP -> innerFindAllCollectionsByAddress<RecAppCollectionDao>(address).map {
            it.asGeneric().apply {
                if (type == CollectionType.UNKNOWN) {
                    postAndSaveUnknownCollectionItem(address, it)
                }
            }
        }
    }

    private fun postAndSaveUnknownCollectionItem(address: Address, collection: CollectionDao) {

        val unknownItem = when (collection) {
            is RecAppCollectionDao -> UnknownCollectionItem(
                address.fullAddress,
                collection.timestamp.toInstant().toLocalDateTime(TimeZone.currentSystemDefault()).toYyyyMmDd(),
                "type: ${collection.fraction.name.nl} || logoId: ${collection.fraction.logo.id}"
            )
            is LimNetCollectionDao -> UnknownCollectionItem(
                address.fullAddress,
                collection.date,
                "category: ${collection.category} || description: ${collection.description}"
            )
        }

        unknownItemDb.find<UnknownCollectionItem>().all().use {
            val list = it.useModels { it.toList() }
            if (list.contains(unknownItem)) {
                Napier.d("DB already contains unknownCollectionItem: $unknownItem")
            } else {
                unknownItemRepository.postUnknownCollection(unknownItem)
                unknownItemDb.put(unknownItem)
            }
        }
    }

    private inline fun <reified T : Any> innerFindAllCollectionsByAddress(address: Address): List<T> {
        val db = getDb(address.faction)
       return db.find<T>().byIndex("addressId", address.id).use {
            it.useModels { it.toList() }
        }
    }

    fun storeCollections(collections: List<CollectionDao>) {
        when (collections.firstOrNull()) {
            is RecAppCollectionDao -> {
                val db = getDb(Faction.RECAPP)
                collections.forEach { db.put(it) }
            }
            is LimNetCollectionDao -> {
                val db = getDb(Faction.LIMNET)
                collections.forEach { db.put(it) }
            }
            else -> {
                Napier.w("Cannot storeCollection for type of ${collections.firstOrNull()}")
            }
        }
    }

    fun deleteAllCollectionsByAddress(address: Address) {
        val db = getDb(address.faction)

        when (address.faction) {
            Faction.LIMNET -> db.find<LimNetCollectionDao>()
                .byIndex("addressId", address.id)
                .use { db.deleteAll(it) }
            Faction.RECAPP -> db.find<RecAppCollectionDao>()
                .byIndex("addressId", address.id)
                .use { db.deleteAll(it) }
        }
    }

    fun findNotificationPropsByAddress(address: Address) = getDb(address.faction)
        .find<be.sigmadelta.common.notifications.NotifProps>()
        .byIndex("addressId", address.id)
        .use {
            it.model()
        }

    fun saveNotificationProps(newProps: be.sigmadelta.common.notifications.NotifProps, address: Address) {
        val db = getDb(address.faction)
        db.find<be.sigmadelta.common.notifications.NotifProps>().byIndex("addressId", address.id).use {
            if (it.isValid()) {
                db.delete(it.key())
            }
        }
        db.put(newProps)
    }

    fun markNotificationTriggered(notificationLabel: NotificationLabel) {
        notificationLabelDb.put(notificationLabel)
    }

    fun getTriggeredNotificationLabels() = notificationLabelDb.find<NotificationLabel>().all().use {
        it.useModels { it.toList() }
    }

    fun getDb(faction: Faction): DB = when (faction) {
        Faction.RECAPP -> recAppDb
        Faction.LIMNET -> limNetDb
    }

    fun migrate() {

        Napier.d("Migrating legacy data")
        legacyRecappDb.find<LegacyAddress>().all().use { addr ->
            addr.useModels { it.toList() }.forEach {
                Napier.d("Migrating RecApp address: $it")
                storeAddress(it.toRecappAddressDao())
            }
        }

        legacyRecappDb.find<NotificationProps>().all().use { props ->
            Napier.d("is valid: ${props.isValid()}")

            props.useModels { it.toList() }.apply { Napier.d("size = $size") }.forEach {
                Napier.d("Migrating RecApp notification props: $it")
                recAppDb.put(it.toNotificationProps())
            }
        }

        legacyNotificationDb.find<NotificationLabel>().all().use { labels ->
            labels.useModels { it.toList() }.forEach {
                Napier.d("Migrating notification label: $it")
                notificationLabelDb.put(it)
            }
        }
    }

    private val recAppDb = DB.factory
        .inDir(getApplicationFilesDirectoryPath())
        .open("bc_recapp_db", TypeTable {
            root<RecAppAddressDao>()
            root<be.sigmadelta.common.notifications.NotifProps>()
        }, org.kodein.db.orm.kotlinx.KotlinxSerializer())

    private val limNetDb = DB.factory
        .inDir(getApplicationFilesDirectoryPath())
        .open("bc_limnet_db", TypeTable(), org.kodein.db.orm.kotlinx.KotlinxSerializer())

    private val notificationLabelDb = DB.factory
        .inDir(getApplicationFilesDirectoryPath())
        .open("bc_notif_label_db", TypeTable {
            root<NotificationLabel>()
        }, org.kodein.db.orm.kotlinx.KotlinxSerializer())

    private val unknownItemDb = DB.factory
        .inDir(getApplicationFilesDirectoryPath())
        .open("bc_unknown_item_db", TypeTable(), org.kodein.db.orm.kotlinx.KotlinxSerializer())

    private val legacyRecappDb = DB.factory
        .inDir(getApplicationFilesDirectoryPath())
        .open("becycle_db", TypeTable {
            root<RecAppAddressDao>()
            root<be.sigmadelta.common.notifications.NotifProps>()
        }, org.kodein.db.orm.kotlinx.KotlinxSerializer()) //TODO: Add for debugging ',LevelDBOptions.PrintLogs(true))'

    private val legacyNotificationDb = DB.factory
        .inDir(getApplicationFilesDirectoryPath())
        .open("becycle_notification_db", TypeTable {
            root<NotificationLabel>()
        }, org.kodein.db.orm.kotlinx.KotlinxSerializer())
}