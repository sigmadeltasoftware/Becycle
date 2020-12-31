package be.sigmadelta.common.address.limnet

import be.sigmadelta.common.util.ApiResponse
import com.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class LimNetAddressApi(
    private val baseUrl: String,
    private val client: HttpClient,
) {

    suspend fun getMunicipalities(searchQuery: String) = client
        .getList(LimNetMunicipalityDao.serializer()) {
            url("$baseUrl/${MUNICIPALITY_API}")
            parameter("query", searchQuery)
        }

    suspend fun getStreets(searchQuery: String, municipality: LimNetMunicipalityDao) = client
        .getList(LimNetStreetDao.serializer()) {
            url("$baseUrl/${STREETS_API(municipality.nisCode)}")
            parameter("query", searchQuery)
        }

    suspend fun getHouseNumbers(searchQuery: String, street: LimNetStreetDao) = client
        .getList(LimNetHouseNumberDao.serializer()) {
            url("$baseUrl/${HOUSENUMBER_API(street.nummer)}")
            parameter("query", searchQuery)
        }

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private suspend inline fun <T> HttpClient.getList(
        serializer: KSerializer<T>,
        request: HttpRequestBuilder.() -> Unit
    ) = try {
        val response = get<HttpResponse> { request.invoke(this) }
        val list = json.decodeFromString(ListSerializer(elementSerializer = serializer), response.readText())
        Napier.d("response = $list")

        ApiResponse.Success(list)
    } catch (e: Throwable) {
        Napier.e("${e.message}\n${e.stackTraceToString()}")
        ApiResponse.Error(e)
    }

    companion object {
        private const val MUNICIPALITY_API = "afval-kalender/gemeenten/search"
        private fun STREETS_API(nisCode: String) = "afval-kalender/gemeente/$nisCode/straten/search"
        private fun HOUSENUMBER_API(streetNumber: String) =
            "afval-kalender/straat/$streetNumber/huisnummers/search"
    }
}