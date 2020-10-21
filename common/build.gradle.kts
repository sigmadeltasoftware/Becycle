plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("org.jetbrains.kotlin.native.cocoapods")
}

android {
    compileSdkVersion(Versions.compileSdk)
    buildToolsVersion(Versions.buildTools)

    defaultConfig {
        minSdkVersion(Versions.minSdk)
        targetSdkVersion(Versions.targetSdk)
        versionCode = Properties.versionCode
        versionName = Properties.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            minifyEnabled(false)
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = listOf("-Xallow-jvm-ir-dependencies", "-Xskip-prerelease-check")
    }
}

// CocoaPods requires the podspec to have a version.
version = "1.0"

kotlin {
    targets {
        val sdkName: String? = System.getenv("SDK_NAME")

        val isiOSDevice = sdkName.orEmpty().startsWith("iphoneos")
        if (isiOSDevice) {
            iosArm64("iOS64")
        } else {
            iosX64("iOS")
        }

        android()
    }


    cocoapods {
        summary = "Becycle common module"
        homepage = "homepage placeholder"
    }

    sourceSets {
        val commonMain by getting {
            dependencies {

                // Kotlin
                api(Kotlin.coroutines)
                api(Kotlin.serialization)
                api(Kotlin.datetime)

                // Ktor
                api(Ktor.core)
                api(Ktor.json)
                api(Ktor.logging)
                api(Ktor.serialization)

                // Koin
                api(Koin.core)
                api(Koin.android)
                api(Koin.androidViewModel)

                // Kodein-DB
                api (Kodein_db.main)
                api (Kodein_db.serializer_kotlinx)

                // Util
                api (Util.slf4j) // Necessary for HttpClient logging
                api (Util.prefs) // Multiplatform preferences
                api (Util.flowTuple)
            }
        }

        val androidMain by getting {
            dependencies {
                api(Ktor.android)
                api(Android.workmanager)
                api(Util.autoStarter)
            }
        }

        val iOSMain by getting {
            dependencies {
                api(Ktor.ios)
            }
        }
    }
}
