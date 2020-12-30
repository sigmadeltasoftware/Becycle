package be.sigmadelta.common.util

import be.sigmadelta.common.Faction
import be.sigmadelta.common.address.Address
import be.sigmadelta.common.address.AddressDao
import be.sigmadelta.common.address.RecAppAddressDao
import be.sigmadelta.common.address.recapp.legacy.LegacyRecappAddress
import be.sigmadelta.common.collections.Collection
import be.sigmadelta.common.collections.recapp.RecAppCollectionDao
import be.sigmadelta.common.db.getApplicationFilesDirectoryPath
import be.sigmadelta.common.notifications.LegacyNotificationProps
import be.sigmadelta.common.notifications.NotificationLabel
import be.sigmadelta.common.notifications.NotificationProps
import org.kodein.db.*
import org.kodein.db.impl.factory
import org.kodein.memory.use

class DBManager {

    inline fun <reified T : Any> findAll(faction: Faction): List<T> = getDb(faction).find<T>()
        .all().use {
            it.useModels { it.toList() }
        }

    fun <T> storeAll(items: List<T>, faction: Faction) {
        val db = getDb(faction)
        items.forEach {
            db.put(it!!)
        }
    }

    fun storeAddress(address: AddressDao) = when (address) {
        is RecAppAddressDao -> recAppDb.put(address)
    }

    fun removeAddress(address: Address) {
        val db = getDb(address.faction)
        when (address.faction) {
            Faction.LIMNET, // TODO
            Faction.RECAPP -> {
                db.find<RecAppAddressDao>().byId(address.id).use {
                    if (it.isValid()) {
                        db.delete(it.key())
                    }
                }
            }
        }
    }

    fun updateAddress(address: AddressDao) = when (address) {
        is RecAppAddressDao -> {
            recAppDb.find<RecAppAddressDao>().byId(address.id).use {
                if (it.isValid()) {
                    recAppDb.deleteAll(it)
                    recAppDb.put(address)
                }
            }
        }
    }

    fun findAllAddresses(): List<Address> {
        val addresses = mutableListOf<Address>()
        Faction.values().forEach {
            when (it) {
                Faction.LIMNET, // TODO
                Faction.RECAPP -> addresses.addAll(findAll<RecAppAddressDao>(it).map { it.asGeneric() })
            }
        }
        return addresses
    }

    fun findAllCollectionsByAddress(address: Address): List<Collection> = when (address.faction) {
        Faction.LIMNET, // TODO
        Faction.RECAPP -> {
            val db = getDb(Faction.RECAPP)
            db.find<RecAppCollectionDao>().byIndex("addressId", address.id).use {
                it.useModels { it.toList().map { it.asGeneric() } }
            }
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

    fun deleteAllCollectionsByAddress(address: Address) {
        val db = getDb(address.faction)

        when (address.faction) {
            Faction.LIMNET, // TODO
            Faction.RECAPP -> db.find<RecAppCollectionDao>()
                .byIndex("addressId", address.id)
                .use { db.deleteAll(it) }
        }
    }

    fun migrate() {
        legacyRecappDb.find<LegacyRecappAddress>().all().use { addr ->
            addr.useModels { it.toList() }.forEach {
                storeAddress(it.toRecappAddressDao())
            }
        }

        legacyNotificationDb.find<LegacyNotificationProps>().all().use { props ->
            props.useModels { it.toList() }.forEach {
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