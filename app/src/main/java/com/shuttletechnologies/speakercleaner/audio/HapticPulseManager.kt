package com.shuttletechnologies.speakercleaner.audio

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HapticPulseManager(private val context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var pulseJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun startWaterEjectVibration() {
        stopVibration()
        pulseJob = scope.launch {
            while (isActive) {
                // Rhythmic resonance pulses
                vibrateBurst(durationMs = 180, amplitude = 255)
                delay(120)
                vibrateBurst(durationMs = 220, amplitude = 200)
                delay(150)
                vibrateBurst(durationMs = 80, amplitude = 255)
                delay(200)
            }
        }
    }

    fun startDustBlastVibration() {
        stopVibration()
        pulseJob = scope.launch {
            while (isActive) {
                // High frequency sharp micro-bursts
                vibrateBurst(durationMs = 40, amplitude = 255)
                delay(50)
                vibrateBurst(durationMs = 40, amplitude = 255)
                delay(50)
                vibrateBurst(durationMs = 40, amplitude = 255)
                delay(300)
            }
        }
    }

    fun triggerClick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (_: Exception) {}
    }

    fun triggerHeavyClick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateBurst(durationMs: Long, amplitude: Int) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val clampedAmp = amplitude.coerceIn(1, 255)
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, clampedAmp))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    fun stopVibration() {
        pulseJob?.cancel()
        pulseJob = null
        try {
            vibrator?.cancel()
        } catch (_: Exception) {}
    }
}
