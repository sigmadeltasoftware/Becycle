import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id ("com.google.firebase.crashlytics")
    kotlin("android")
    kotlin("android.extensions")
}

android {
    compileSdkVersion(Versions.compileSdk)
    buildToolsVersion(Versions.buildTools)

    signingConfigs {
        getByName("debug") {
            storeFile = file("/Users/sigmadelta/SANDBOX/Becycle/app/becycle.keystore")
            storePassword = "Becycle!KeyStore"
            keyAlias = "key1"
            keyPassword = "Becycle!KeyStore"
        }
        create("release") {
            val props = Properties()
            var propsFile = File("$rootDir/app", "keystore.properties")
            println("Propsfile exists = ${propsFile.exists()}")
            props.load(propsFile.inputStream())
            storeFile = File("$rootDir/app", props["storeFile"] as String)
            storePassword = props["storePassword"] as String
            keyAlias = props["keyAlias"] as String
            keyPassword = props["keyPassword"] as String
            println("""
                storeFile = $storeFile
                storePassword = $storePassword
                keyAlias = $keyAlias
                keyPassword = $keyPassword
            """.trimIndent())
        }
    }

    defaultConfig {
        applicationId = BecProperties.applicationId
        minSdkVersion(Versions.minSdk)
        targetSdkVersion(Versions.targetSdk)
        vectorDrawables.useSupportLibrary = true

        versionCode = BecProperties.versionCode
        versionName = BecProperties.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    composeOptions {
        kotlinCompilerVersion = Versions.kotlin
        kotlinCompilerExtensionVersion = Versions.compose
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
            isShrinkResources = false
        }

        getByName("release") {
            // TODO: Reenable minification once deps are a bit more mature (KodeinDB, ...)
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packagingOptions {
        exclude("META-INF/*.kotlin_module")
    }

    lintOptions {
        disable("InvalidFragmentVersionForActivityResult")
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = listOf("-Xallow-jvm-ir-dependencies", "-Xskip-prerelease-check")
    }
}

dependencies {
    implementation(project(":app:common"))
    // Necessary for Kotlinx.datetime / java.time libraries in sub API 26 devices
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.1")
}
