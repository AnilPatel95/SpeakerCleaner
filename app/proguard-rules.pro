# ProGuard / R8 Shrinking & Obfuscation Rules
# ----------------------------------------------------

# Keep standard Android entry points and components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.view.View

# Keep manifest resource references intact
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Jetpack Compose specific rules
-keepclassmembers class * extends androidx.compose.runtime.snapshots.SnapshotMutableState { *; }

# Google Mobile Ads (AdMob)
-keep public class com.google.android.gms.ads.** {
   public *;
}
-dontwarn com.google.android.gms.ads.**
-keep public class com.google.ads.** {
   public *;
}

# AndroidX Biometric
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# AndroidX WorkManager & Room (Used by AdMob background processing)
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class androidx.room.** { *; }
-keep class **_Impl { *; }
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
-keep class androidx.startup.** { *; }
-dontwarn androidx.room.**
-dontwarn androidx.work.**
-dontwarn androidx.startup.**

# Keep App Data Models
-keep class com.shuttletechnologies.speakercleaner.data.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
