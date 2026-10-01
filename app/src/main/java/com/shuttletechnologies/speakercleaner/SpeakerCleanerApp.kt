package com.shuttletechnologies.speakercleaner

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

class SpeakerCleanerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Ensure Firebase is initialized
        FirebaseApp.initializeApp(this)

        // Configure Firebase Crashlytics
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCrashlyticsCollectionEnabled(true)
        crashlytics.setCustomKey("version_code", BuildConfig.VERSION_CODE)
        crashlytics.setCustomKey("version_name", BuildConfig.VERSION_NAME)
        crashlytics.setCustomKey("build_type", if (BuildConfig.DEBUG) "debug" else "release")
        crashlytics.log("SpeakerCleanerApp initialized")

        // Initialize Google Mobile Ads SDK on background thread
        MobileAds.initialize(this) {}

        // Set test device configuration for safety
        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(listOf("B3EEABB8EE11C2BE770B684D95219ECB"))
            .build()
        MobileAds.setRequestConfiguration(configuration)
    }
}
