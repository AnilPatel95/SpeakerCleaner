package com.shuttletechnologies.speakercleaner.audio

import android.content.Context
import android.media.AudioManager
import com.shuttletechnologies.speakercleaner.data.SpeakerTarget

class AudioOutputRouter(context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var originalMode = audioManager.mode
    private var originalSpeakerphoneOn = audioManager.isSpeakerphoneOn

    @Suppress("DEPRECATION")
    fun routeTo(target: SpeakerTarget) {
        try {
            when (target) {
                SpeakerTarget.LOUDSPEAKER -> {
                    audioManager.mode = AudioManager.MODE_NORMAL
                    audioManager.isSpeakerphoneOn = true
                }
                SpeakerTarget.EARPIECE -> {
                    audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                    audioManager.isSpeakerphoneOn = false
                }
            }
        } catch (_: Exception) {}
    }

    @Suppress("DEPRECATION")
    fun restoreOriginalRouting() {
        try {
            audioManager.isSpeakerphoneOn = originalSpeakerphoneOn
            audioManager.mode = originalMode
        } catch (_: Exception) {}
    }

    fun getMaxMusicVolume(): Int {
        return try {
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        } catch (_: Exception) { 15 }
    }

    fun getCurrentMusicVolume(): Int {
        return try {
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        } catch (_: Exception) { 10 }
    }

    fun setMaxVolume() {
        try {
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0)
        } catch (_: Exception) {}
    }
}
