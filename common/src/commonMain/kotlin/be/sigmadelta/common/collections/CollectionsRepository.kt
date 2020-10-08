package be.sigmadelta.common.collections

import be.sigmadelta.common.address.Address
import be.sigmadelta.common.util.ApiResponse
import be.sigmadelta.common.util.Response
import be.sigmadelta.common.util.networkBoundResource
import kotlinx.coroutines.flow.Flow
import org.kodein.db.DB
import org.kodein.db.deleteAll
import org.kodein.db.find
import org.kodein.db.useModels

class CollectionsRepository(private val db: DB, private val collectionsApi: CollectionsApi) {

    suspend fun searchCollections(
        address: Address,
        currentMonth: Int
    ): Flow<Response<List<Collection>>> = networkBoundResource(
        shouldFetch = {
            it.firstOrNull { it.timestamp.substringAfter("-").substringBefore("-").toInt() == currentMonth } == null
        },
        query = {
            db.find<Collection>().all()
            .useModels { it.toList() }
        },
        fetch = {
            collectionsApi.getCollections(address, currentMonth)
        },
        saveFetchResult = { result -> when(result) {
            is ApiResponse.Success -> insertCollections(result.body.items.map { it.copy(addressId = address.id) })
            is ApiResponse.Error -> Response.Error<List<Collection>>(result.error)
        }}
    )

    private fun insertCollections(collection: List<Collection>) {
        db.deleteAll(db.find<Collection>().all())
        collection.forEach { db.put(it) }
    }
}
