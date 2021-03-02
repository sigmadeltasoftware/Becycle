# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# KOIN
-keepnames class android.arch.lifecycle.ViewModel -keepclassmembers public class * extends android.arch.lifecycle.ViewModel { public <init>(...); }
-keepclassmembers class com.lebao.app.domain.** { public <init>(...); }
-keepclassmembers class * { public <init>(...); }

# Kodein DB
-keepattributes Signature
-keepclassmembers class org.kodein.db.**

## Kotlinx serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class be.sigmadelta.becycle.**$$serializer { *; } # <-- change package name to your app's
-keepclassmembers class be.sigmadelta.becycle.** { # <-- change package name to your app's
    *** Companion;
}
-keepclasseswithmembers class be.sigmadelta.becycle.** { # <-- change package name to your app's
    kotlinx.serialization.KSerializer serializer(...);
}