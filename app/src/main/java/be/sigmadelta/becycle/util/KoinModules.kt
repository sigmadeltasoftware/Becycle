package be.sigmadelta.becycle.util

import android.app.PendingIntent
import android.content.Intent
import androidx.compose.material.ExperimentalMaterialApi
import be.sigmadelta.becycle.BuildConfig
import be.sigmadelta.becycle.SplashScreenActivity
import be.sigmadelta.becycle.baseheaders.BaseHeadersViewModel
import be.sigmadelta.becycle.address.AddressViewModel
import be.sigmadelta.becycle.accesstoken.AccessTokenViewModel
import be.sigmadelta.becycle.collections.CollectionsViewModel
import be.sigmadelta.becycle.common.analytics.AnalyticsTracker
import be.sigmadelta.becycle.notification.NotificationViewModel
import be.sigmadelta.common.Preferences
import be.sigmadelta.common.baseheader.RecAppBaseHeadersApi
import be.sigmadelta.common.accesstoken.AccessTokenApi
import be.sigmadelta.common.address.recapp.RecAppAddressApi
import be.sigmadelta.common.address.AddressRepository
import be.sigmadelta.common.baseheader.BaseHeadersRepository
import be.sigmadelta.common.accesstoken.AccessTokenRepository
import be.sigmadelta.common.address.limnet.LimNetAddressApi
import be.sigmadelta.common.collections.recapp.RecAppCollectionsApi
import be.sigmadelta.common.collections.CollectionsRepository
import be.sigmadelta.common.collections.limnet.LimNetCollectionsApi
import be.sigmadelta.common.notifications.NotificationRepo
import be.sigmadelta.common.util.AuthorizationKeyExpiredException
import be.sigmadelta.common.util.DBManager
import be.sigmadelta.common.util.InvalidAddressException
import be.sigmadelta.common.util.SessionStorage
import be.sigmadelta.common.util.unknownitem.UnknownItemApi
import be.sigmadelta.common.util.unknownitem.UnknownItemRepository
import com.google.firebase.analytics.FirebaseAnalytics
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

private const val RECYCLE_BASE_URL = "https://recycleapp.be/api/app/v1"
private const val LIMNET_BASE_URL = "https://limburg.net/api-proxy/public"

@ExperimentalMaterialApi
val coreModule = module {
    single { client }
    single { UnknownItemApi(get()) }
    single { UnknownItemRepository(get()) }
    single { DBManager(get()) }
    single { Preferences() }
    single { PendingIntent.getActivity(androidContext(), 0, Intent(androidContext(), SplashScreenActivity::class.java), 0) }
    single { NotificationRepo(androidContext(), get()) }
    single { AnalyticsTracker(FirebaseAnalytics.getInstance(androidContext())) }
    viewModel { NotificationViewModel(get(), get()) }
}

val sessionStorage = module {
    single { SessionStorage() }
}

val baseHeadersModule = module {
    single { RecAppBaseHeadersApi(get()) }
    single { BaseHeadersRepository(get()) }
    viewModel { BaseHeadersViewModel(get(), get()) }
}

val recycleModule = module {
    single { AccessTokenApi(RECYCLE_BASE_URL, get(), get()) }
    single { RecAppAddressApi(RECYCLE_BASE_URL, get(), get()) }
    single { LimNetAddressApi(LIMNET_BASE_URL, get()) }
    single { LimNetCollectionsApi(LIMNET_BASE_URL, get()) }
    single { AccessTokenRepository(get()) }
    single { AddressRepository(get(), get(), get(), get()) }
    viewModel { AddressViewModel(get(), get(), get()) }
    viewModel { AccessTokenViewModel(get(), get()) }
}

val collectionsModule = module {
    single { RecAppCollectionsApi(RECYCLE_BASE_URL, get(), get()) }
    single { CollectionsRepository(get(), get(), get()) }
    viewModel { CollectionsViewModel(get(), get()) }
}

private val client = HttpClient {
    install(JsonFeature) {
        serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.INFO
    }
    HttpResponseValidator {
        validateResponse { response ->
            val statusCode = response.status.value
            when (statusCode) {
                in 300..399 -> throw RedirectResponseException(response)
                500 -> throw InvalidAddressException(response)
                401 -> throw AuthorizationKeyExpiredException(response)
                in 400..499 -> throw ClientRequestException(response)
                in 500..599 -> throw ServerResponseException(response)
            }

            if (statusCode >= 600) {
                throw ResponseException(response)
            }
        }
    }
}