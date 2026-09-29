package com.shuttletechnologies.speakercleaner.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("speaker_cleaner_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(prefs.getInt(KEY_THEME_MODE, 0))
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    private val _languageCode = MutableStateFlow(prefs.getString(KEY_LANGUAGE, "en_US") ?: "en_US")
    val languageCode: StateFlow<String> = _languageCode.asStateFlow()

    private val _isSecurityEnabled = MutableStateFlow(prefs.getBoolean(KEY_SECURITY_ENABLED, false))
    val isSecurityEnabled: StateFlow<Boolean> = _isSecurityEnabled.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false))
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _hasRated = MutableStateFlow(prefs.getBoolean(KEY_HAS_RATED, false))
    val hasRated: StateFlow<Boolean> = _hasRated.asStateFlow()

    private val _sessions = MutableStateFlow<List<CleaningSession>>(loadSessions())
    val sessions: StateFlow<List<CleaningSession>> = _sessions.asStateFlow()

    fun setThemeMode(mode: Int) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun setLanguageCode(code: String) {
        prefs.edit().putString(KEY_LANGUAGE, code).apply()
        _languageCode.value = code
    }

    fun setSecurityEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SECURITY_ENABLED, enabled).apply()
        _isSecurityEnabled.value = enabled
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        _isBiometricEnabled.value = enabled
        if (enabled && !_isSecurityEnabled.value) {
            setSecurityEnabled(true)
        }
    }

    fun setPin(pin: String) {
        val hash = hashPin(pin)
        prefs.edit().putString(KEY_PIN_HASH, hash).apply()
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return storedHash == hashPin(pin)
    }

    fun hasPinConfigured(): Boolean {
        return !prefs.getString(KEY_PIN_HASH, null).isNullOrBlank()
    }

    fun setHasRated(rated: Boolean) {
        prefs.edit().putBoolean(KEY_HAS_RATED, rated).apply()
        _hasRated.value = rated
    }

    fun recordSession(session: CleaningSession) {
        val currentList = _sessions.value.toMutableList()
        currentList.add(0, session)
        if (currentList.size > 50) {
            currentList.removeAt(currentList.lastIndex)
        }
        _sessions.value = currentList
        saveSessions(currentList)

        // Increment stats
        val total = prefs.getInt(KEY_TOTAL_CLEANS, 0) + 1
        val water = if (session.mode == CleanMode.WATER_EJECT || session.mode == CleanMode.DEEP_CLEAN) {
            prefs.getInt(KEY_WATER_CLEANS, 0) + 1
        } else {
            prefs.getInt(KEY_WATER_CLEANS, 0)
        }
        val dust = if (session.mode == CleanMode.DUST_BLAST) {
            prefs.getInt(KEY_DUST_CLEANS, 0) + 1
        } else {
            prefs.getInt(KEY_DUST_CLEANS, 0)
        }

        prefs.edit()
            .putInt(KEY_TOTAL_CLEANS, total)
            .putInt(KEY_WATER_CLEANS, water)
            .putInt(KEY_DUST_CLEANS, dust)
            .putLong(KEY_LAST_CLEAN_TIME, session.timestamp)
            .apply()
    }

    fun getTotalCleans(): Int = prefs.getInt(KEY_TOTAL_CLEANS, 0)
    fun getWaterCleans(): Int = prefs.getInt(KEY_WATER_CLEANS, 0)
    fun getDustCleans(): Int = prefs.getInt(KEY_DUST_CLEANS, 0)
    fun getLastCleanTime(): Long = prefs.getLong(KEY_LAST_CLEAN_TIME, 0L)

    private fun loadSessions(): List<CleaningSession> {
        val jsonStr = prefs.getString(KEY_SESSIONS_JSON, null) ?: return emptyList()
        val list = mutableListOf<CleaningSession>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CleaningSession(
                        id = obj.getString("id"),
                        timestamp = obj.getLong("timestamp"),
                        mode = CleanMode.valueOf(obj.getString("mode")),
                        target = SpeakerTarget.valueOf(obj.getString("target")),
                        durationSeconds = obj.getInt("durationSeconds"),
                        initialDb = if (obj.has("initialDb")) obj.getDouble("initialDb").toFloat() else null,
                        finalDb = if (obj.has("finalDb")) obj.getDouble("finalDb").toFloat() else null,
                        completed = obj.getBoolean("completed")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun saveSessions(list: List<CleaningSession>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("timestamp", item.timestamp)
            obj.put("mode", item.mode.name)
            obj.put("target", item.target.name)
            obj.put("durationSeconds", item.durationSeconds)
            item.initialDb?.let { obj.put("initialDb", it.toDouble()) }
            item.finalDb?.let { obj.put("finalDb", it.toDouble()) }
            obj.put("completed", item.completed)
            array.put(obj)
        }
        prefs.edit().putString(KEY_SESSIONS_JSON, array.toString()).apply()
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LANGUAGE = "selected_language"
        private const val KEY_SECURITY_ENABLED = "security_enabled"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_HAS_RATED = "has_rated"
        private const val KEY_SESSIONS_JSON = "sessions_json"
        private const val KEY_TOTAL_CLEANS = "total_cleans"
        private const val KEY_WATER_CLEANS = "water_cleans"
        private const val KEY_DUST_CLEANS = "dust_cleans"
        private const val KEY_LAST_CLEAN_TIME = "last_clean_time"
    }
}
