package be.sigmadelta.common.collections

import be.sigmadelta.common.address.Address
import be.sigmadelta.common.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.*
import org.kodein.db.DB
import org.kodein.db.deleteAll
import org.kodein.db.find
import org.kodein.db.useModels

class CollectionsRepository(private val db: DB, private val collectionsApi: CollectionsApi) {

    suspend fun searchUpcomingCollections(
        address: Address,
        referenceDate: Instant = Clock.System.now()
    ): Flow<Response<List<Collection>>> = networkBoundResource(
        shouldFetch = { list ->
            (list.filter {
                println("it: ${it.timestamp.toInstant().toEpochMilliseconds()} > referenceDate: ${referenceDate.toEpochMilliseconds()}")
                it.timestamp.toInstant().toEpochMilliseconds() > referenceDate.toEpochMilliseconds()
            }.size < SIZE_UPCOMING_ITEMS_RETURN).apply {
                println("searchUpcomingCollections - shouldFetch = $this")
            }
        },
        query = {
            val list = db.find<Collection>().all()
                .useModels { it.toList() }
                .filter { it.timestamp.toInstant() > referenceDate }
                .filter { it.addressId == address.id }.apply {
                    println("searchUpcomingCollections - query = $this")
                }
                .sortedBy { it.timestamp.toInstant() }
            if (list.size > SIZE_UPCOMING_ITEMS_RETURN) {
                list.subList(0, SIZE_UPCOMING_ITEMS_RETURN)
            } else list
        },
        fetch = {
            val date = referenceDate.toLocalDateTime(TimeZone.currentSystemDefault())
            val untilMonth = if (date.monthNumber == 12) 1 else date.monthNumber + 1
            val untilYear = if (date.monthNumber == 12) date.year + 1 else date.year
            val untilDate = LocalDateTime(untilYear, untilMonth, date.dayOfMonth, 0, 0, 0, 0).toYyyyMmDd()

            collectionsApi.getCollections(address, date.toYyyyMmDd(), untilDate, 100)
        },
        saveFetchResult = { result ->
            when (result) {
                is ApiResponse.Success -> storeCollections(result.body.items.map {
                    it.copy(
                        addressId = address.id
                    )
                })
                is ApiResponse.Error -> Response.Error<List<Collection>>(result.error)
            }
        }
    )

    private fun storeCollections(collection: List<Collection>) {
        db.deleteAll(db.find<Collection>().all())
        collection.forEach { db.put(it) }
    }

    companion object {
        private const val SIZE_UPCOMING_ITEMS_RETURN = 6
    }
}

