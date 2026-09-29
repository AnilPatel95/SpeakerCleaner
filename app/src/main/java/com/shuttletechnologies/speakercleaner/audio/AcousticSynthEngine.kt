package com.shuttletechnologies.speakercleaner.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.shuttletechnologies.speakercleaner.data.WaveformType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.floor
import kotlin.math.sin
import kotlin.random.Random

class AcousticSynthEngine {
    private val sampleRate = 44100
    private var audioTrack: AudioTrack? = null
    private var synthJob: Job? = null
    private val synthScope = CoroutineScope(Dispatchers.Default)

    @Volatile
    private var isPlaying = false

    @Volatile
    var targetFrequency: Float = 165f

    @Volatile
    var currentFrequency: Float = 165f

    @Volatile
    var waveform: WaveformType = WaveformType.SINE

    @Volatile
    var volumeLevel: Float = 1.0f // 0.0 to 1.0

    @Volatile
    var stereoPan: Float = 0.0f // -1.0 (Left), 0.0 (Center), 1.0 (Right)

    @Volatile
    var noiseMode: NoiseMode = NoiseMode.NONE

    private val _liveFrequency = MutableStateFlow(165f)
    val liveFrequency: StateFlow<Float> = _liveFrequency.asStateFlow()

    private val _isPlayingState = MutableStateFlow(false)
    val isPlayingState: StateFlow<Boolean> = _isPlayingState.asStateFlow()

    private val _currentRms = MutableStateFlow(0f)
    val currentRms: StateFlow<Float> = _currentRms.asStateFlow()

    enum class NoiseMode {
        NONE, WHITE, PINK
    }

    // Pink noise filter state
    private var b0 = 0.0
    private var b1 = 0.0
    private var b2 = 0.0
    private var b3 = 0.0
    private var b4 = 0.0
    private var b5 = 0.0
    private var b6 = 0.0

    fun startPlayback(useVoiceCallStream: Boolean = false) {
        if (isPlaying) return
        isPlaying = true
        _isPlayingState.value = true

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = minBufferSize * 2

        val streamType = if (useVoiceCallStream) {
            AudioManager.STREAM_VOICE_CALL
        } else {
            AudioManager.STREAM_MUSIC
        }

        val usage = if (useVoiceCallStream) {
            AudioAttributes.USAGE_VOICE_COMMUNICATION
        } else {
            AudioAttributes.USAGE_MEDIA
        }

        val contentType = if (useVoiceCallStream) {
            AudioAttributes.CONTENT_TYPE_SPEECH
        } else {
            AudioAttributes.CONTENT_TYPE_MUSIC
        }

        val attributes = AudioAttributes.Builder()
            .setUsage(usage)
            .setContentType(contentType)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .build()

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(attributes)
            .setAudioFormat(format)
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()

        synthJob = synthScope.launch {
            val chunkSamples = 1024
            val buffer = ShortArray(chunkSamples * 2) // Stereo (L, R)
            var phase = 0.0

            while (isActive && isPlaying) {
                // Smooth frequency slewing
                currentFrequency += (targetFrequency - currentFrequency) * 0.08f
                _liveFrequency.value = currentFrequency

                val twoPi = 2.0 * PI
                val phaseIncrement = (twoPi * currentFrequency) / sampleRate

                var sumSquares = 0.0

                for (i in 0 until chunkSamples) {
                    val sample: Double = when (noiseMode) {
                        NoiseMode.WHITE -> {
                            Random.nextDouble(-1.0, 1.0)
                        }
                        NoiseMode.PINK -> {
                            val white = Random.nextDouble(-1.0, 1.0)
                            b0 = 0.99886 * b0 + white * 0.0555179
                            b1 = 0.99332 * b1 + white * 0.0750759
                            b2 = 0.96900 * b2 + white * 0.1538520
                            b3 = 0.86650 * b3 + white * 0.3104856
                            b4 = 0.55000 * b4 + white * 0.5329522
                            b5 = -0.7616 * b5 - white * 0.0168980
                            val pink = (b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362) * 0.11
                            b6 = white * 0.115926
                            pink.coerceIn(-1.0, 1.0)
                        }
                        NoiseMode.NONE -> {
                            when (waveform) {
                                WaveformType.SINE -> sin(phase)
                                WaveformType.SQUARE -> if (sin(phase) >= 0.0) 0.9 else -0.9
                                WaveformType.TRIANGLE -> (2.0 / PI) * asin(sin(phase).coerceIn(-1.0, 1.0))
                                WaveformType.SAWTOOTH -> {
                                    val norm = (phase / twoPi) - floor(0.5 + phase / twoPi)
                                    2.0 * norm
                                }
                            }
                        }
                    }

                    phase += phaseIncrement
                    if (phase >= twoPi) phase -= twoPi

                    val leftMultiplier = ((1.0 - stereoPan) / 2.0).coerceIn(0.0, 1.0)
                    val rightMultiplier = ((1.0 + stereoPan) / 2.0).coerceIn(0.0, 1.0)

                    val scaledSample = (sample * volumeLevel).coerceIn(-1.0, 1.0)
                    val leftShort = (scaledSample * leftMultiplier * 32767.0).toInt().toShort()
                    val rightShort = (scaledSample * rightMultiplier * 32767.0).toInt().toShort()

                    buffer[i * 2] = leftShort
                    buffer[i * 2 + 1] = rightShort

                    sumSquares += (scaledSample * scaledSample)
                }

                audioTrack?.write(buffer, 0, buffer.size)

                val rms = kotlin.math.sqrt(sumSquares / chunkSamples).toFloat()
                _currentRms.value = rms
            }
        }
    }

    fun stopPlayback() {
        isPlaying = false
        _isPlayingState.value = false
        _currentRms.value = 0f
        synthJob?.cancel()
        synthJob = null

        try {
            audioTrack?.apply {
                pause()
                flush()
                stop()
                release()
            }
        } catch (_: Exception) {}
        audioTrack = null
    }

    fun release() {
        stopPlayback()
    }
}
