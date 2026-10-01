package com.shuttletechnologies.speakercleaner.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shuttletechnologies.speakercleaner.audio.AcousticSynthEngine
import com.shuttletechnologies.speakercleaner.audio.AudioOutputRouter
import com.shuttletechnologies.speakercleaner.audio.HapticPulseManager
import com.shuttletechnologies.speakercleaner.data.WaveformType
import com.shuttletechnologies.speakercleaner.localization.appStrings
import com.shuttletechnologies.speakercleaner.theme.LocalAppColors
import com.shuttletechnologies.speakercleaner.ui.components.SmallNativeAdView
import com.shuttletechnologies.speakercleaner.ui.components.WaveformVisualizer
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun ManualToneScreen(
    synthEngine: AcousticSynthEngine,
    audioRouter: AudioOutputRouter,
    hapticManager: HapticPulseManager,
    onActiveStateChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = appStrings()
    val view = LocalView.current
    val scrollState = rememberScrollState()

    var frequency by remember { mutableFloatStateOf(440f) }
    var selectedWaveform by remember { mutableStateOf(WaveformType.SINE) }
    var volume by remember { mutableFloatStateOf(0.85f) }

    val isPlaying by synthEngine.isPlayingState.collectAsState()
    val liveFreq by synthEngine.liveFrequency.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            if (synthEngine.isPlayingState.value) {
                synthEngine.stopPlayback()
                onActiveStateChanged(false)
            }
        }
    }

    fun togglePlayback() {
        if (isPlaying) {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
            synthEngine.stopPlayback()
            onActiveStateChanged(false)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            audioRouter.setMaxVolume()
            synthEngine.targetFrequency = frequency
            synthEngine.currentFrequency = frequency
            synthEngine.waveform = selectedWaveform
            synthEngine.volumeLevel = volume
            synthEngine.stereoPan = 0f
            synthEngine.noiseMode = AcousticSynthEngine.NoiseMode.NONE
            synthEngine.startPlayback(useVoiceCallStream = false)
            onActiveStateChanged(true)
        }
    }

    fun adjustFrequency(delta: Float) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        val newFreq = (frequency + delta).coerceIn(1f, 22000f)
        frequency = newFreq
        synthEngine.targetFrequency = newFreq
    }

    // Convert linear slider 0..1 to logarithmic frequency 1..22000 Hz
    val minFreq = 1.0
    val maxFreq = 22000.0
    val sliderPosition = (log10(frequency.toDouble()) - log10(minFreq)) / (log10(maxFreq) - log10(minFreq))

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Waveform Visualizer
        WaveformVisualizer(
            frequency = if (isPlaying) liveFreq else frequency,
            waveform = selectedWaveform,
            isPlaying = isPlaying
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Large Frequency Readout
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(vertical = 14.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = strings.frequencyLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textMuted
            )
            Text(
                text = "${frequency.toInt()} Hz",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = colors.accent
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Logarithmic Frequency Slider
            Slider(
                value = sliderPosition.toFloat().coerceIn(0f, 1f),
                onValueChange = { pos ->
                    val logMin = log10(minFreq)
                    val logMax = log10(maxFreq)
                    val targetLog = logMin + (pos * (logMax - logMin))
                    val calculatedFreq = (10.0.pow(targetLog)).toFloat().coerceIn(1f, 22000f)
                    frequency = calculatedFreq
                    synthEngine.targetFrequency = calculatedFreq
                },
                colors = SliderDefaults.colors(
                    thumbColor = colors.accent,
                    activeTrackColor = colors.accent,
                    inactiveTrackColor = colors.border
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Precision Stepper Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StepperPill(label = "-100", onClick = { adjustFrequency(-100f) })
                StepperPill(label = "-10", onClick = { adjustFrequency(-10f) })
                StepperPill(label = "-1", onClick = { adjustFrequency(-1f) })
                StepperPill(label = "+1", onClick = { adjustFrequency(1f) })
                StepperPill(label = "+10", onClick = { adjustFrequency(10f) })
                StepperPill(label = "+100", onClick = { adjustFrequency(100f) })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Waveform Selector Chips
        Text(
            text = strings.waveformTitle,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textMuted,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WaveformType.values().forEach { wave ->
                val isSelected = selectedWaveform == wave
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) colors.accent.copy(alpha = 0.18f) else colors.surfaceElevated,
                    animationSpec = tween(200),
                    label = "wave_bg"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .border(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) colors.accent else colors.border.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            selectedWaveform = wave
                            synthEngine.waveform = wave
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = wave.getLocalizedName(strings),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) colors.accent else colors.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Acoustic Presets
        Text(
            text = strings.presetsTitle,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textMuted,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetChip(
                label = strings.presetWaterShort,
                isSelected = frequency.toInt() == 165,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    frequency = 165f
                    selectedWaveform = WaveformType.SINE
                    synthEngine.targetFrequency = 165f
                    synthEngine.waveform = WaveformType.SINE
                },
                modifier = Modifier.weight(1f)
            )

            PresetChip(
                label = strings.presetPitchShort,
                isSelected = frequency.toInt() == 440,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    frequency = 440f
                    synthEngine.targetFrequency = 440f
                },
                modifier = Modifier.weight(1f)
            )

            PresetChip(
                label = strings.presetCleanShort,
                isSelected = frequency.toInt() == 1000,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    frequency = 1000f
                    synthEngine.targetFrequency = 1000f
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Volume Output Slider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "${(volume * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                modifier = Modifier.width(40.dp)
            )
            Slider(
                value = volume,
                onValueChange = { vol ->
                    volume = vol
                    synthEngine.volumeLevel = vol
                },
                colors = SliderDefaults.colors(
                    thumbColor = colors.accent,
                    activeTrackColor = colors.accent,
                    inactiveTrackColor = colors.border
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Start / Stop Tone Button
        Button(
            onClick = { togglePlayback() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPlaying) colors.danger else colors.accent,
                contentColor = if (isPlaying) androidx.compose.ui.graphics.Color.White else (if (colors.isDark) android.graphics.Color.BLACK.let { androidx.compose.ui.graphics.Color(it) } else androidx.compose.ui.graphics.Color.White)
            )
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isPlaying) strings.btnStopTone else strings.btnPlayTone,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Small Native Ad at Bottom
        SmallNativeAdView()

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StepperPill(
    label: String,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.background)
            .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun PresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) colors.accent.copy(alpha = 0.18f) else colors.surfaceElevated)
            .border(
                if (isSelected) 1.5.dp else 1.dp,
                if (isSelected) colors.accent else colors.border.copy(alpha = 0.5f),
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) colors.accent else colors.textPrimary,
            maxLines = 1
        )
    }
}
