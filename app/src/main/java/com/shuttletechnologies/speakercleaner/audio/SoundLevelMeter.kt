package com.shuttletechnologies.speakercleaner.audio

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sqrt

class SoundLevelMeter(private val context: Context) {
    private val sampleRate = 44100
    private var audioRecord: AudioRecord? = null
    private var meterJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _currentDb = MutableStateFlow(30f)
    val currentDb: StateFlow<Float> = _currentDb.asStateFlow()

    private val _peakDb = MutableStateFlow(30f)
    val peakDb: StateFlow<Float> = _peakDb.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun startListening() {
        if (_isRecording.value || !hasRecordPermission()) return

        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                max(minBufferSize, 2048)
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord?.release()
                audioRecord = null
                return
            }

            audioRecord?.startRecording()
            _isRecording.value = true

            meterJob = scope.launch {
                val buffer = ShortArray(1024)
                var peak = 30f

                while (isActive && _isRecording.value) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        var sum = 0.0
                        for (i in 0 until read) {
                            sum += buffer[i] * buffer[i]
                        }
                        val rms = sqrt(sum / read)
                        // Calibrate roughly to dB SPL: 20 * log10(rms) + reference offset
                        val db = if (rms > 0.0) {
                            (20.0 * log10(rms) + 15.0).toFloat().coerceIn(25f, 105f)
                        } else {
                            25f
                        }

                        // Smooth filter
                        val smoothed = _currentDb.value + (db - _currentDb.value) * 0.25f
                        _currentDb.value = smoothed

                        if (smoothed > peak) {
                            peak = smoothed
                        } else {
                            peak = max(30f, peak - 0.05f)
                        }
                        _peakDb.value = peak
                    }
                }
            }
        } catch (_: Exception) {
            stopListening()
        }
    }

    fun stopListening() {
        _isRecording.value = false
        meterJob?.cancel()
        meterJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
    }

    fun resetPeak() {
        _peakDb.value = 30f
    }
}
