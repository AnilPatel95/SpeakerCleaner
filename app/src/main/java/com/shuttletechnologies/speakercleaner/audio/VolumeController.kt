package com.shuttletechnologies.speakercleaner.audio

import android.content.Context
import android.media.AudioManager

class VolumeController(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var originalMusicVolume: Int? = null
    private var originalVoiceVolume: Int? = null

    fun maximizeVolumeForCleaning(isVoiceCall: Boolean = false) {
        val stream = if (isVoiceCall) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC
        val currentVolume = audioManager.getStreamVolume(stream)
        val maxVolume = audioManager.getStreamMaxVolume(stream)

        if (isVoiceCall) {
            if (originalVoiceVolume == null) originalVoiceVolume = currentVolume
        } else {
            if (originalMusicVolume == null) originalMusicVolume = currentVolume
        }

        try {
            audioManager.setStreamVolume(stream, maxVolume, 0)
        } catch (_: Exception) {}
    }

    fun restoreOriginalVolume(isVoiceCall: Boolean = false) {
        val stream = if (isVoiceCall) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC
        val savedVolume = if (isVoiceCall) originalVoiceVolume else originalMusicVolume

        if (savedVolume != null) {
            try {
                audioManager.setStreamVolume(stream, savedVolume, 0)
            } catch (_: Exception) {}
            if (isVoiceCall) originalVoiceVolume = null else originalMusicVolume = null
        }
    }
}
