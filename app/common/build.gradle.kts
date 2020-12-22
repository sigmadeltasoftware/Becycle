plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(Versions.compileSdk)
    buildToolsVersion(Versions.buildTools)

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    androidExtensions {
        features = setOf("parcelize")
    }

    composeOptions {
        kotlinCompilerVersion = Versions.kotlin
        kotlinCompilerExtensionVersion = Versions.compose
    }

    buildFeatures {
        compose = true
    }

    // Needed to enforce androidx.core:core version (1.5.0-alpha3) for DisplayInsets.kt
    configurations.all {
        resolutionStrategy.force(Android.core)
    }

//    configure(android.lintOptions) {
//        // Disable this once KMM is not alpha anymore, now it triggers too much errors on external dependencies
//        abortOnError = false
//    }
}

// Add against compose errors with kotlin compilers
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = listOf("-Xallow-jvm-ir-dependencies", "-Xskip-prerelease-check")
    }
}

dependencies {
    api(project(":common"))

    api(Android.appcompat)
    api(Android.core)
    api(Android.core_ktx)
    api(Android.material)
    api(Android.lifecycle_extensions)
    api(Android.lifecycle_viewmodel_ktx)

    api(Compose.ui)
    api(Compose.uiGraphics)
    api(Compose.uiTooling)
    api(Compose.foundationLayout)
    api(Compose.material)
    api(Compose.runtimeLiveData)
    api(Compose.compiler)

    api(AndroidUi.material_dialogs_core)
    api(AndroidUi.material_dialogs_bottomsheet)
    api(AndroidUi.calpose)

    api(platform(Firebase.bom))
    api(Firebase.analytics)
    api(Firebase.crashlytics)
}

