package com.shuttletechnologies.speakercleaner

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

class SpeakerCleanerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Google Mobile Ads SDK on background thread
        MobileAds.initialize(this) {}

        // Set test device configuration for safety
        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(listOf("B3EEABB8EE11C2BE770B684D95219ECB"))
            .build()
        MobileAds.setRequestConfiguration(configuration)
    }
}
