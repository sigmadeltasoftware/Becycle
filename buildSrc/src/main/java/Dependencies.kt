object Properties {
    const val applicationId = "be.sigmadelta.becycle"
    const val versionCode = 1
    const val versionName = "1.0"
}

object Versions {
    const val gradleTools = "4.2.0-alpha13"
    const val compileSdk = 30
    const val targetSdk = compileSdk
    const val minSdk = 23
    const val buildTools = "$compileSdk.0.2"

    const val kotlin = "1.4.10"
    const val kotlinCoroutines = "1.3.9-native-mt-2"
    const val ktor = "1.4.0"
    const val kotlinxSerialization = "1.0.0-RC"
    const val koin = "3.0.0-alpha-4"
    const val core = "1.5.0-alpha02"
    const val core_ktx = core
    const val lifecycle = "2.2.0"
    const val compose = "1.0.0-alpha04"
    const val junit = "4.12"
    const val material = "1.2.1"
    const val appcompat = "1.2.0"
    const val kodein_db = "0.3.0-beta"
    const val slf4j = "1.7.30"
    const val datetime = "0.1.0"
    const val settings_prefs = "0.6.2"
}

object Kotlin {
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}"
    const val serialization = "org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.kotlinxSerialization}"
    const val datetime = "org.jetbrains.kotlinx:kotlinx-datetime:${Versions.datetime}"
}

object Ktor {
    const val core = "io.ktor:ktor-client-core:${Versions.ktor}"
    const val json = "io.ktor:ktor-client-json:${Versions.ktor}"
    const val logging = "io.ktor:ktor-client-logging:${Versions.ktor}"
    const val serialization = "io.ktor:ktor-client-serialization:${Versions.ktor}"
    const val android = "io.ktor:ktor-client-android:${Versions.ktor}"
    const val ios = "io.ktor:ktor-client-ios:${Versions.ktor}"
}

object Compose {
    const val ui = "androidx.compose.ui:ui:${Versions.compose}"
    const val uiGraphics = "androidx.compose.ui:ui-graphics:${Versions.compose}"
    const val uiTooling = "androidx.ui:ui-tooling:${Versions.compose}"
    const val foundationLayout = "androidx.compose.foundation:foundation-layout:${Versions.compose}"
    const val material = "androidx.compose.material:material:${Versions.compose}"
    const val runtimeLiveData =  "androidx.compose.runtime:runtime-livedata:${Versions.compose}"
    const val compiler = "androidx.compose.compiler:compiler:${Versions.compose}"
}

object Android {
    const val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    const val core = "androidx.core:core:${Versions.core_ktx}"
    const val core_ktx = "androidx.core:core-ktx:${Versions.core_ktx}"
    const val material = "com.google.android.material:material:${Versions.material}"
    const val lifecycle_extensions = "androidx.lifecycle:lifecycle-extensions:${Versions.lifecycle}"
    const val lifecycle_viewmodel_ktx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
}

object Koin {
    const val core = "org.koin:koin-core:${Versions.koin}"
    const val android = "org.koin:koin-android:${Versions.koin}"
    const val androidViewModel = "org.koin:koin-androidx-viewmodel:${Versions.koin}"
}

object Kodein_db {
    const val main = "org.kodein.db:kodein-db:${Versions.kodein_db}"
    const val serializer_kotlinx = "org.kodein.db:kodein-db-serializer-kotlinx:${Versions.kodein_db}"
}

object Util {
    const val slf4j = "org.slf4j:slf4j-simple:${Versions.slf4j}"
    const val prefs = "com.russhwolf:multiplatform-settings-no-arg:${Versions.settings_prefs}"

}
