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

class CollectionsRepository(
    private val dbMan: DBManager,
    private val collectionsApi: RecAppCollectionsApi
) {

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
            val list = dbMan.findAllCollectionsByAddress(address)
                .filter { it.date >= referenceDate.toLocalDateTime(TimeZone.currentSystemDefault()) }
                .sortedBy { it.date }
                .apply {
                    if (size > TOTAL_ITEMS_RETURN) {
                        subList(0, TOTAL_ITEMS_RETURN)
                    } else this
                }

            val today = list.filter { it.date.isToday() }
            val tomorrow = list.filter { it.date.isTomorrow() }
            val todayTomorrow = today.toMutableList().apply { addAll(tomorrow) }
            val upcoming = list.toMutableList().apply { removeAll(todayTomorrow) }
            val upcomingSubListSize =
                if (upcoming.size > UPCOMING_MAX_SIZE) UPCOMING_MAX_SIZE else upcoming.size
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
        },
        fetch = {
            val date = referenceDate.toLocalDateTime(TimeZone.currentSystemDefault()).toYyyyMmDd()
            val datePlus2Months =
                referenceDate.plus(DateTimePeriod(months = 2), TimeZone.currentSystemDefault())
                    .toLocalDateTime(TimeZone.currentSystemDefault())
            val untilDate = LocalDateTime(
                datePlus2Months.year,
                datePlus2Months.month,
                1,
                1, 1, 1, 1
            ).toYyyyMmDd()

            when (address.faction) {
                Faction.LIMNET, // TODO
                Faction.RECAPP -> {
                    dbMan.findAll<RecAppAddressDao>(address.faction)
                        .firstOrNull { it.id == address.id }?.let {
                            collectionsApi.getCollections(it, date, untilDate, 40)
                        }
                }
            }
        },
        saveFetchResult = { result ->
            when (result) {
                is ApiResponse.Success -> storeCollections(result.body.items, address)
                is ApiResponse.Error -> Response.Error<List<RecAppCollectionDao>>(result.error)
            }
        }
    )

    suspend fun removeCollections(address: Address) {
        dbMan.deleteAllCollectionsByAddress(address)
    }

    private fun storeCollections(collection: List<RecAppCollectionDao>, address: Address) {
        // TODO: Find a more efficient way to store collection without having to delete all prior ones before
        dbMan.deleteAllCollectionsByAddress(address)
        dbMan.storeAll(collection, address.faction)
    }

    private fun <T> List<T>.nullOnEmpty() = if (isEmpty()) null else this

    companion object {
        private const val TOTAL_ITEMS_RETURN = 12
        private const val UPCOMING_MAX_SIZE = 10
    }
}
