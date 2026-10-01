package com.shuttletechnologies.speakercleaner.util

import com.google.firebase.crashlytics.FirebaseCrashlytics

object CrashlyticsHelper {
    private val crashlytics: FirebaseCrashlytics by lazy {
        FirebaseCrashlytics.getInstance()
    }

    fun log(message: String) {
        try {
            crashlytics.log(message)
        } catch (_: Exception) {}
    }

    fun recordException(throwable: Throwable) {
        try {
            crashlytics.recordException(throwable)
        } catch (_: Exception) {}
    }

    fun setCustomKey(key: String, value: String) {
        try {
            crashlytics.setCustomKey(key, value)
        } catch (_: Exception) {}
    }

    fun setCustomKey(key: String, value: Boolean) {
        try {
            crashlytics.setCustomKey(key, value)
        } catch (_: Exception) {}
    }

    fun setCustomKey(key: String, value: Int) {
        try {
            crashlytics.setCustomKey(key, value)
        } catch (_: Exception) {}
    }
}
