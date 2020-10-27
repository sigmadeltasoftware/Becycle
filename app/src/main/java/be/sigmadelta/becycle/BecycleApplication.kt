package be.sigmadelta.becycle

import android.app.Application
import be.sigmadelta.becycle.util.*
import be.sigmadelta.common.db.appCtx
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.EmptyLogger

class BecycleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        appCtx = this

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