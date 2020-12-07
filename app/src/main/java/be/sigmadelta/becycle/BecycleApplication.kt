package be.sigmadelta.becycle

import android.app.Application
import androidx.compose.ui.focus.ExperimentalFocus
import be.sigmadelta.becycle.util.*
import be.sigmadelta.common.db.appCtx
import be.sigmadelta.common.util.initLogger
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.EmptyLogger

@ExperimentalFocus
class BecycleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(BuildConfig.DEBUG.not())

        appCtx = this
        initLogger()

        startKoin {
            androidLogger()
            EmptyLogger()
            androidContext(this@BecycleApplication)
            modules(
                coreModule,
                sessionStorage,
                baseHeadersModule,
                recycleModule,
                collectionsModule
            )
        }
    }
}