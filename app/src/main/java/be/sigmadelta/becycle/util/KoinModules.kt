package be.sigmadelta.becycle.util

import android.app.PendingIntent
import android.content.Intent
import androidx.compose.material.ExperimentalMaterialApi
import be.sigmadelta.becycle.SplashScreenActivity
import be.sigmadelta.becycle.baseheaders.BaseHeadersViewModel
import be.sigmadelta.becycle.address.AddressViewModel
import be.sigmadelta.becycle.accesstoken.AccessTokenViewModel
import be.sigmadelta.becycle.collections.CollectionsViewModel
import be.sigmadelta.becycle.common.analytics.AnalyticsTracker
import be.sigmadelta.becycle.notification.NotificationViewModel
import be.sigmadelta.common.Preferences
import be.sigmadelta.common.address.RecAppAddressDao
import be.sigmadelta.common.db.getApplicationFilesDirectoryPath
import be.sigmadelta.common.baseheader.RecAppBaseHeadersApi
import be.sigmadelta.common.accesstoken.AccessTokenApi
import be.sigmadelta.common.address.recapp.RecAppAddressApi
import be.sigmadelta.common.address.AddressRepository
import be.sigmadelta.common.baseheader.BaseHeadersRepository
import be.sigmadelta.common.accesstoken.AccessTokenRepository
import be.sigmadelta.common.collections.recapp.RecAppCollectionsApi
import be.sigmadelta.common.collections.CollectionsRepository
import be.sigmadelta.common.notifications.NotificationLabel
import be.sigmadelta.common.notifications.NotificationProps
import be.sigmadelta.common.notifications.NotificationRepo
import be.sigmadelta.common.util.AuthorizationKeyExpiredException
import be.sigmadelta.common.util.InvalidAddressException
import be.sigmadelta.common.util.SessionStorage
import com.google.firebase.analytics.FirebaseAnalytics
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import org.kodein.db.DB
import org.kodein.db.TypeTable
import org.kodein.db.impl.factory
import org.kodein.db.inDir
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

private const val RECYCLE_BASE_URL = "https://recycleapp.be/api/app/v1"
private val recAppDb = DB.factory
    .inDir(getApplicationFilesDirectoryPath())
    .open("becycle_db", TypeTable {
        root<RecAppAddressDao>()
        root<NotificationProps>()
    }, org.kodein.db.orm.kotlinx.KotlinxSerializer())

private val notificationDb = DB.factory
    .inDir(getApplicationFilesDirectoryPath())
    .open("becycle_notification_db", TypeTable {
        root<NotificationLabel>()
    }, org.kodein.db.orm.kotlinx.KotlinxSerializer())

@ExperimentalMaterialApi
val coreModule = module {
    single { recAppDb }
    single { Preferences() }
    single { PendingIntent.getActivity(androidContext(), 0, Intent(androidContext(), SplashScreenActivity::class.java), 0) }
    single { NotificationRepo(androidContext(), get(), notificationDb) }
    single { AnalyticsTracker(FirebaseAnalytics.getInstance(androidContext())) }
    viewModel { NotificationViewModel(get(), get()) }
}

val sessionStorage = module {
    single { SessionStorage() }
    single { client }
}

val baseHeadersModule = module {
    single { RecAppBaseHeadersApi(get()) }
    single { BaseHeadersRepository(get()) }
    viewModel { BaseHeadersViewModel(get(), get()) }
}

val recycleModule = module {
    single { AccessTokenApi(RECYCLE_BASE_URL, get(), get()) }
    single { RecAppAddressApi(RECYCLE_BASE_URL, get(), get()) }
    single { AccessTokenRepository(get()) }
    single { AddressRepository(recAppDb, get(), get()) }
    viewModel { AddressViewModel(get(), get(), get()) }
    viewModel { AccessTokenViewModel(get(), get()) }
}

val collectionsModule = module {
    single { RecAppCollectionsApi(RECYCLE_BASE_URL, get(), get()) }
    single { CollectionsRepository(recAppDb, get()) }
    viewModel { CollectionsViewModel(get(), get()) }
}

private val client = HttpClient {
    install(JsonFeature) {
        serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
            isLenient = true; ignoreUnknownKeys = true
        })
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
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