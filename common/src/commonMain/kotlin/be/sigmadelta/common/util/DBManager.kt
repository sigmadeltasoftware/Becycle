package be.sigmadelta.common.util

import be.sigmadelta.common.Faction
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.address.AddressDao
import be.sigmadelta.common.address.LimNetAddressDao
import be.sigmadelta.common.address.RecAppAddressDao
import be.sigmadelta.common.address.recapp.legacy.LegacyRecappAddress
import be.sigmadelta.common.collections.*
import be.sigmadelta.common.collections.Collection as TrashCollection
import be.sigmadelta.common.db.getApplicationFilesDirectoryPath
import be.sigmadelta.common.notifications.LegacyNotificationProps
import be.sigmadelta.common.notifications.NotificationLabel
import be.sigmadelta.common.notifications.NotificationProps
import be.sigmadelta.common.util.unknownitem.UnknownItemRepository
import com.github.aakira.napier.Napier
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
            is LimNetAddressDao -> db.find<RecAppAddressDao>().byId(address.id).use {
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
                    unknownItemRepository.postUnknownCollection(address, it)
                }
            }
        }
        Faction.RECAPP -> innerFindAllCollectionsByAddress<RecAppCollectionDao>(address).map {
            it.asGeneric().apply {
                if (type == CollectionType.UNKNOWN) {
                    unknownItemRepository.postUnknownCollection(address, it)
                }
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
        .find<NotificationProps>()
        .byIndex("addressId", address.id)
        .use {
            it.model()
        }

    fun saveNotificationProps(newProps: NotificationProps, address: Address) {
        val db = getDb(address.faction)
        db.find<NotificationProps>().byIndex("addressId", address.id).use {
            if (it.isValid()) {
                db.delete(it.key())
            }
        }
        db.put(newProps)
    }

    fun markNotificationTriggered(notificationLabel: NotificationLabel) {
        notificationDb.put(notificationLabel)
    }

    fun getTriggeredNotificationLabels() = notificationDb.find<NotificationLabel>().all().use {
        it.useModels { it.toList() }
    }

    fun getDb(faction: Faction): DB = when (faction) {
        Faction.RECAPP -> recAppDb
        Faction.LIMNET -> limNetDb
    }

    fun migrate() {
        Napier.d("Migrating legacy data")
        legacyRecappDb.find<LegacyRecappAddress>().all().use { addr ->
            addr.useModels { it.toList() }.forEach {
                Napier.d("Migrating RecApp address: $it")
                storeAddress(it.toRecappAddressDao())
            }
        }

        legacyNotificationDb.find<LegacyNotificationProps>().all().use { props ->
            props.useModels { it.toList() }.forEach {
                Napier.d("Migrating RecApp notification props: $it")
                recAppDb.put(it.toNotificationProps())
            }
        }
    }

    private val recAppDb = DB.factory
        .inDir(getApplicationFilesDirectoryPath())
        .open("bc_recapp_db", TypeTable {
            root<RecAppAddressDao>()
            root<NotificationProps>()
        }, org.kodein.db.orm.kotlinx.KotlinxSerializer())

    private val limNetDb = DB.factory
        .inDir(getApplicationFilesDirectoryPath())
        .open("bc_limnet_db", TypeTable(), org.kodein.db.orm.kotlinx.KotlinxSerializer())

    private val notificationDb = DB.factory
        .inDir(getApplicationFilesDirectoryPath())
        .open("bc_notif_db", TypeTable {
            root<NotificationLabel>()
        }, org.kodein.db.orm.kotlinx.KotlinxSerializer())

    private val legacyRecappDb = DB.factory
        .inDir(getApplicationFilesDirectoryPath())
        .open("becycle_db", TypeTable {
            root<RecAppAddressDao>()
            root<NotificationProps>()
        }, org.kodein.db.orm.kotlinx.KotlinxSerializer())

    private val legacyNotificationDb = DB.factory
        .inDir(getApplicationFilesDirectoryPath())
        .open("becycle_notification_db", TypeTable {
            root<NotificationLabel>()
        }, org.kodein.db.orm.kotlinx.KotlinxSerializer())
}