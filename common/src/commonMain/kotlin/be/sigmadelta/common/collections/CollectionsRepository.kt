package be.sigmadelta.common.collections

import be.sigmadelta.common.address.Address
import be.sigmadelta.common.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import org.kodein.db.DB
import org.kodein.db.deleteAll
import org.kodein.db.find
import org.kodein.db.useModels

class CollectionsRepository(private val db: DB, private val collectionsApi: CollectionsApi) {

    suspend fun searchUpcomingCollections(
        address: Address,
        referenceDate: Instant = Clock.System.now()
    ): Flow<Response<CollectionOverview>> = networkBoundResource(
        shouldFetch = { overview ->
            (overview.upcoming?.filter {
                println(
                    "it: ${
                        it.timestamp.toInstant().toEpochMilliseconds()
                    } > referenceDate: ${referenceDate.toEpochMilliseconds()}"
                )
                it.timestamp.toInstant().toEpochMilliseconds() > referenceDate.toEpochMilliseconds()
            }?.size ?: 0 +
                    (overview.today?.size ?: 0) +
                    (overview.tomorrow?.size ?: 0)
                    < TOTAL_ITEMS_RETURN).apply {
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
                .apply {
                    if (size > TOTAL_ITEMS_RETURN) {
                        subList(0, TOTAL_ITEMS_RETURN)
                    } else this
                }
            val today = list.filter {
                it.timestamp.toInstant().toLocalDateTime(TimeZone.currentSystemDefault()).isToday()
            }
            val tomorrow = list.filter {
                it.timestamp.toInstant().toLocalDateTime(TimeZone.currentSystemDefault())
                    .isTomorrow()
            }
            val todayTomorrow = today.toMutableList().apply { addAll(tomorrow) }
            val upcoming = list.toMutableList().apply { removeAll(todayTomorrow) }
            CollectionOverview(
                today.nullOnEmpty(),
                tomorrow.nullOnEmpty(),
                upcoming.nullOnEmpty()
            )
        },
        fetch = {
            val date = referenceDate.toLocalDateTime(TimeZone.currentSystemDefault()).toYyyyMmDd()
            val untilDate =
                referenceDate.plus(DateTimePeriod(months = 1), TimeZone.currentSystemDefault())
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .toYyyyMmDd()

            collectionsApi.getCollections(address, date, untilDate, 100)
        },
        saveFetchResult = { result ->
            when (result) {
                is ApiResponse.Success -> storeCollections(result.body.items, address.id)
                is ApiResponse.Error -> Response.Error<List<Collection>>(result.error)
            }
        }
    )

    suspend fun removeCollections(address: Address) {
        db.deleteAll(db.find<Collection>().byIndex("addressId", address.id))
    }

    private fun storeCollections(collection: List<Collection>, addressId: String) {
        db.deleteAll(db.find<Collection>().byIndex("addressId", addressId))
        collection.forEach { db.put(it) }
    }

    private fun <T> List<T>.nullOnEmpty() = if (isEmpty()) null else this

    companion object {
        private const val TOTAL_ITEMS_RETURN = 8
    }
}
