package be.sigmadelta.common.collections.recapp

import be.sigmadelta.common.address.RecAppAddressDao
import be.sigmadelta.common.collections.CollectionException
import be.sigmadelta.common.collections.RecAppCollectionDao
import be.sigmadelta.common.util.ApiResponse
import be.sigmadelta.common.util.SearchQueryResult
import be.sigmadelta.common.util.SessionStorage
import be.sigmadelta.common.util.TranslationContainer
import com.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class RecAppCollectionsApi(
    private val baseUrl: String,
    private val client: HttpClient,
    private val sessionStorage: SessionStorage
) {

    suspend fun getCollections(
        address: RecAppAddressDao,
        fromDateYyyyMmDd: String,
        untilDateYyyyMmDd: String,
        size: Int
    ): ApiResponse<List<RecAppCollectionDao>> = try {
        val response = client.get<HttpResponse> {
            url("$baseUrl/$COLLECTIONS_API")
            sessionStorage.attachHeaders(this)
            url.encodedPath =
                "${url.encodedPath}?zipcodeId=${address.zipCodeItem.id}&streetId=${address.street.id}&fromDate=$fromDateYyyyMmDd&untilDate=$untilDateYyyyMmDd&houseNumber=${address.houseNumber}&size=$size"
            // NOTE: I had to construct the url manually here as Ktor would use character encoding on the streetId parameter, which would cause the API request to fail
            // creating the request post encoding, makes sure that the parameter stays intact
        }

        val json = Json{ignoreUnknownKeys = true; isLenient = true}
        val surrogate = json.decodeFromString(SearchQueryResult.serializer(typeSerial0 = JsonObject.serializer()), response.readText())

        val collections = mutableListOf<RecAppCollectionResponse>()
        surrogate.items.forEach {
            // See bottom of file for more info
            if (it.containsKey(EVENT_KEY).not()) {
                if (it.containsKey(EXCEPTION_KEY)) {
                    val exceptionObj = it[EXCEPTION_KEY]?.jsonObject
                    Napier.d("exceptionObj = $exceptionObj")
                    // Add to collections if it doesn't contain a 'replacedBy' field in the exceptions object
                    if (exceptionObj?.containsKey(REPLACEDBY_KEY) == false) {
                        collections.add(json.decodeFromJsonElement(RecAppCollectionResponse.serializer(), it))
                    }
                } else {
                    collections.add(json.decodeFromJsonElement(RecAppCollectionResponse.serializer(), it))
                }
            } else {
                Napier.d("Disregarding collection with 'event' type: ${it}")
            }
        }

        Napier.v("response = $response")
        Napier.v("surrogate = $surrogate")
        val collectionsMapped = collections.map { it.toCollection(address) }
        Napier.v("collections = $collections")
        Napier.v("collectionsMapped = $collectionsMapped")

        ApiResponse.Success(collectionsMapped)
    } catch (e: Throwable) {
        Napier.e("${e.message}\n${e.stackTraceToString()}")
        ApiResponse.Error(e)
    }

    companion object {
        const val COLLECTIONS_API = "collections"
        private const val EXCEPTION_KEY = "exception"
        private const val EVENT_KEY = "event"
        private const val REPLACEDBY_KEY = "replacedBy"
    }
}

@Serializable
data class RecAppCollectionResponse (
    val timestamp: String,
    val type: String,
    val fraction: RecAppCollectionFractionDao,
    val exception: RecAppCollectionExceptionDao? = null
) {
    fun toCollection(address: RecAppAddressDao) = RecAppCollectionDao(
        timestamp = this.timestamp,
        fraction = this.fraction,
        type = this.type,
        addressId = address.id,
        exception = this.exception
    )
}

@Serializable
data class RecAppCollectionExceptionDao(
    val reason: RecAppCollectionExceptionReasonDao,
    val replaces: RecAppCollectionExceptionReplacesDao
) {
    fun toCollectionException() = CollectionException(
        title = reason.name.nl,
        replacementDate = replaces.timestamp.toInstant().toLocalDateTime(TimeZone.currentSystemDefault())
    )
}

@Serializable
data class RecAppCollectionExceptionReasonDao(
    val id: String,
    val name: TranslationContainer
)

@Serializable
data class RecAppCollectionExceptionReplacesDao(
    val id: String,
    val timestamp: String,
    val fraction: String
)

/*
    Recycle app can return objects that don't contain the default structure, like for example event data (which we filter out for now TODO):

    {
    event: {title: {nl: "mobiel recyclagepark", fr: "mobiel recyclagepark", en: "mobiel recyclagepark",…},…}
    description: {,…}
        de: "Je kan er gratis terecht met kleine hoeveelheden: harde kunststoffen, hout, kaarsresten, keramiek↵& porselein (beschadigde tassen en borden), kga,..."
        en: "Je kan er gratis terecht met kleine hoeveelheden: harde kunststoffen, hout, kaarsresten, keramiek↵& porselein (beschadigde tassen en borden), kga,..."
        fr: "Je kan er gratis terecht met kleine hoeveelheden: harde kunststoffen, hout, kaarsresten, keramiek↵& porselein (beschadigde tassen en borden), kga,..."
        nl: "Je kan er gratis terecht met kleine hoeveelheden: harde kunststoffen, hout, kaarsresten, keramiek↵& porselein (beschadigde tassen en borden), kga,..."
    externalLink: {nl: "https://www.igean.be/mobielrecyclagepark", fr: "https://www.igean.be/mobielrecyclagepark",…}
        de: "https://www.igean.be/mobielrecyclagepark"
        en: "https://www.igean.be/mobielrecyclagepark"
        fr: "https://www.igean.be/mobielrecyclagepark"
        nl: "https://www.igean.be/mobielrecyclagepark"
    introduction: {nl: "Telkens van 9.00 tot 12.00 u aan het Marktplein.",…}
        de: "Telkens van 9.00 tot 12.00 u aan het Marktplein."
        en: "Telkens van 9.00 tot 12.00 u aan het Marktplein."
        fr: "Telkens van 9.00 tot 12.00 u aan het Marktplein."
        nl: "Telkens van 9.00 tot 12.00 u aan het Marktplein."
    title: {nl: "mobiel recyclagepark", fr: "mobiel recyclagepark", en: "mobiel recyclagepark",…}
        de: "mobiel recyclagepark"
        en: "mobiel recyclagepark"
        fr: "mobiel recyclagepark"
        nl: "mobiel recyclagepark"
    fraction: {variations: []}
    variations: []
    id: "5fd87016e79f44db9e098b24"
    timestamp: "2021-01-08T00:00:00.000Z"
    type: "event"
    }

    Or even collection types containing an 'exception'. THese will contain both the collections being replaced as the ones which are replaced.
 */
