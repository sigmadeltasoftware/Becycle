package be.sigmadelta.common.collections

import be.sigmadelta.common.address.Address
import be.sigmadelta.common.Faction
import be.sigmadelta.common.address.RecAppAddressDao
import be.sigmadelta.common.collections.recapp.RecAppCollectionDao
import be.sigmadelta.common.collections.recapp.RecAppCollectionsApi
import be.sigmadelta.common.util.*
import com.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.*
import org.kodein.db.DB
import org.kodein.db.deleteAll
import org.kodein.db.find
import org.kodein.db.useModels

class CollectionsRepository(private val recAppDb: DB, private val collectionsApi: RecAppCollectionsApi) {

    suspend fun searchUpcomingCollections(
        address: Address,
        referenceDate: Instant = yesterday(),
        shouldNotFetch: Boolean = false
    ): Flow<Response<CollectionOverview>> = networkBoundResource(
        shouldFetch = { overview ->
            val upcomingSize = overview.upcoming?.filter {
                it.date.toInstant(TimeZone.currentSystemDefault()) > referenceDate
            }?.size ?: 0

            val todaySize = overview.today?.size ?: 0
            val tomorrowSize = overview.tomorrow?.size ?: 0

            (upcomingSize + todaySize + tomorrowSize < TOTAL_ITEMS_RETURN).apply {
                Napier.d(
                    """
                    searchUpcomingCollections():
                    shouldFetch = $todaySize + $tomorrowSize + $upcomingSize < $TOTAL_ITEMS_RETURN ($this) || shouldNotFetch = $shouldNotFetch
                    ==> shouldFetch = $this && ${shouldNotFetch.not()}
                    """.trimIndent()
                )
            } && shouldNotFetch.not()
        },
        query = {
            when (address.faction) {
                Faction.LIMNET,
                Faction.RECAPP -> {
                    val list = recAppDb.find<RecAppCollectionDao>().all()
                        .useModels { it.toList() }
                        .apply {
                            Napier.d("nonfiltered query = ${this.map { it.timestamp }}")
                        }
                        .filter { it.timestamp.toInstant() >= referenceDate }
                        .filter { it.addressId == address.id }.apply {
                            Napier.d("query = ${this.map { "${it.collectionType}_${it.timestamp}" }}")
                        }
                        .sortedBy { it.timestamp.toInstant() }
                        .apply {
                            if (size > TOTAL_ITEMS_RETURN) {
                                subList(0, TOTAL_ITEMS_RETURN)
                            } else this
                        }.map { it.asGeneric() }

                    val today = list.filter { it.date.isToday() }
                    val tomorrow = list.filter { it.date.isTomorrow() }
                    val todayTomorrow = today.toMutableList().apply { addAll(tomorrow) }
                    val upcoming = list.toMutableList().apply { removeAll(todayTomorrow) }
                    val upcomingSubListSize = if (upcoming.size > 5) 5 else upcoming.size
                    Napier.d(
                        """
                today: size = ${today.size} || $today
                tomorrow: size = ${tomorrow.size} || $tomorrow
                upcoming: size = ${upcoming.size} || $upcoming
            """.trimIndent()
                    )
                    CollectionOverview(
                        today.nullOnEmpty(),
                        tomorrow.nullOnEmpty(),
                        upcoming.subList(0, upcomingSubListSize).nullOnEmpty()
                    )
                }
            }
        },
        fetch = {
            val date = referenceDate.toLocalDateTime(TimeZone.currentSystemDefault()).toYyyyMmDd()
            val datePlus2Months = referenceDate.plus(DateTimePeriod(months = 2), TimeZone.currentSystemDefault())
                .toLocalDateTime(TimeZone.currentSystemDefault())
            val untilDate = LocalDateTime(
                datePlus2Months.year,
                datePlus2Months.month,
                1,
                1,1,1,1
            ).toYyyyMmDd()

            when (address.faction) {
                Faction.LIMNET, // TODO
                Faction.RECAPP -> {
                    recAppDb.find<RecAppAddressDao>().all()
                        .useModels { it.toList() }
                        .firstOrNull { it.id == address.id }?.let {
                            collectionsApi.getCollections(it, date, untilDate, 40)
                        }
                }
            }
        },
        saveFetchResult = { result ->
            when (result) {
                is ApiResponse.Success -> storeCollections(result.body.items, address.id)
                is ApiResponse.Error -> Response.Error<List<RecAppCollectionDao>>(result.error)
            }
        }
    )

    suspend fun removeCollections(address: Address) {
        recAppDb.deleteAll(recAppDb.find<RecAppCollectionDao>().byIndex("addressId", address.id))
    }

    private fun storeCollections(collection: List<RecAppCollectionDao>, addressId: String) {
        recAppDb.deleteAll(recAppDb.find<RecAppCollectionDao>().byIndex("addressId", addressId))
        collection.forEach { recAppDb.put(it) }
    }

    private fun <T> List<T>.nullOnEmpty() = if (isEmpty()) null else this

    companion object {
        private const val TOTAL_ITEMS_RETURN = 8
    }
}
