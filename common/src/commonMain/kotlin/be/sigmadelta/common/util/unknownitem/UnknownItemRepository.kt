package be.sigmadelta.common.util.unknownitem

import be.sigmadelta.common.address.Address
import be.sigmadelta.common.collections.CollectionDao
import be.sigmadelta.common.collections.LimNetCollectionDao
import be.sigmadelta.common.collections.RecAppCollectionDao
import be.sigmadelta.common.util.toYyyyMmDd
import kotlinx.coroutines.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime


class UnknownItemRepository(private val unknownItemApi: UnknownItemApi) {

    fun postUnknownCollection(address: Address, collection: CollectionDao) {
        val collection = when (collection) {
            is RecAppCollectionDao -> UnknownCollectionItem(
                address.fullAddress,
                collection.timestamp.toInstant().toLocalDateTime(TimeZone.currentSystemDefault()).toYyyyMmDd(),
                "collectionType: ${collection.type} || logoId: ${collection.fraction.logo.id}"
            )
            is LimNetCollectionDao -> UnknownCollectionItem(
                address.fullAddress,
                collection.date,
                "category: ${collection.category} || description: ${collection.description}"
            )
        }

        GlobalScope.launch(Dispatchers.Default) {
            unknownItemApi.logUnknownCollection(collection)
        }
    }
}