package com.shuttletechnologies.speakercleaner.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.SurroundSound
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.shuttletechnologies.speakercleaner.audio.AcousticSynthEngine
import com.shuttletechnologies.speakercleaner.audio.AudioOutputRouter
import com.shuttletechnologies.speakercleaner.audio.HapticPulseManager
import com.shuttletechnologies.speakercleaner.audio.SoundLevelMeter
import com.shuttletechnologies.speakercleaner.data.WaveformType
import com.shuttletechnologies.speakercleaner.localization.appStrings
import com.shuttletechnologies.speakercleaner.theme.LocalAppColors
import com.shuttletechnologies.speakercleaner.ui.components.SmallNativeAdView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow

@Composable
fun DiagnosticsScreen(
    synthEngine: AcousticSynthEngine,
    audioRouter: AudioOutputRouter,
    hapticManager: HapticPulseManager,
    soundMeter: SoundLevelMeter,
    onActiveStateChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val strings = appStrings()
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var isSweepRunning by remember { mutableStateOf(false) }
    var sweepProgress by remember { mutableFloatStateOf(0f) }
    var sweepJob by remember { mutableStateOf<Job?>(null) }

    var stereoChannel by remember { mutableStateOf("CENTER") } // LEFT, RIGHT, CENTER
    var activeNoise by remember { mutableStateOf(AcousticSynthEngine.NoiseMode.NONE) }

    var beforeDb by remember { mutableStateOf<Float?>(null) }
    var afterDb by remember { mutableStateOf<Float?>(null) }

    val liveDb by soundMeter.currentDb.collectAsState()
    val peakDb by soundMeter.peakDb.collectAsState()
    val isMeterRecording by soundMeter.isRecording.collectAsState()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted) {
            try {
                soundMeter.startListening()
            } catch (_: Throwable) {}
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isSweepRunning) {
                sweepJob?.cancel()
                synthEngine.stopPlayback()
            }
            if (activeNoise != AcousticSynthEngine.NoiseMode.NONE) {
                synthEngine.stopPlayback()
            }
            soundMeter.stopListening()
            onActiveStateChanged(false)
        }
    }

    fun stopAllAudio() {
        sweepJob?.cancel()
        sweepJob = null
        isSweepRunning = false
        activeNoise = AcousticSynthEngine.NoiseMode.NONE
        synthEngine.stopPlayback()
        onActiveStateChanged(false)
    }

    fun playStereoChannel(pan: Float, channelName: String) {
        stopAllAudio()
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        stereoChannel = channelName
        synthEngine.stereoPan = pan
        synthEngine.targetFrequency = 440f
        synthEngine.currentFrequency = 440f
        synthEngine.waveform = WaveformType.SINE
        synthEngine.volumeLevel = 0.9f
        synthEngine.noiseMode = AcousticSynthEngine.NoiseMode.NONE
        synthEngine.startPlayback()
        onActiveStateChanged(true)
    }

    fun startFrequencySweep() {
        stopAllAudio()
        isSweepRunning = true
        sweepProgress = 0f
        onActiveStateChanged(true)
        synthEngine.stereoPan = 0f
        synthEngine.waveform = WaveformType.SINE
        synthEngine.volumeLevel = 0.95f
        synthEngine.noiseMode = AcousticSynthEngine.NoiseMode.NONE
        synthEngine.targetFrequency = 20f
        synthEngine.currentFrequency = 20f
        synthEngine.startPlayback()

        sweepJob = scope.launch {
            val totalSteps = 150 // 15 seconds duration
            for (step in 0..totalSteps) {
                delay(100)
                val prog = step.toFloat() / totalSteps.toFloat()
                sweepProgress = prog
                // Logarithmic frequency sweep from 20 Hz to 20,000 Hz
                val freq = 20f * (1000f.pow(prog))
                synthEngine.targetFrequency = freq
            }
            stopAllAudio()
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        }
    }

    fun toggleNoise(mode: AcousticSynthEngine.NoiseMode) {
        if (activeNoise == mode) {
            stopAllAudio()
        } else {
            stopAllAudio()
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            activeNoise = mode
            synthEngine.stereoPan = 0f
            synthEngine.volumeLevel = 0.85f
            synthEngine.noiseMode = mode
            synthEngine.startPlayback()
            onActiveStateChanged(true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Stereo Channel Separation Test
        DiagnosticCard(
            title = strings.diagStereoTitle,
            description = strings.diagStereoDesc,
            icon = Icons.Rounded.SurroundSound
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DiagPillButton(
                    label = strings.btnTestLeft,
                    isSelected = stereoChannel == "LEFT" && synthEngine.isPlayingState.collectAsState().value,
                    onClick = { playStereoChannel(-1.0f, "LEFT") },
                    modifier = Modifier.weight(1f)
                )

                DiagPillButton(
                    label = strings.btnTestBoth,
                    isSelected = stereoChannel == "CENTER" && synthEngine.isPlayingState.collectAsState().value,
                    onClick = { playStereoChannel(0.0f, "CENTER") },
                    modifier = Modifier.weight(1f)
                )

                DiagPillButton(
                    label = strings.btnTestRight,
                    isSelected = stereoChannel == "RIGHT" && synthEngine.isPlayingState.collectAsState().value,
                    onClick = { playStereoChannel(1.0f, "RIGHT") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Frequency Sweep Test (20 Hz - 20 kHz)
        DiagnosticCard(
            title = strings.diagSweepTitle,
            description = strings.diagSweepDesc,
            icon = Icons.Rounded.GraphicEq
        ) {
            if (isSweepRunning) {
                LinearProgressIndicator(
                    progress = { sweepProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = colors.accent,
                    trackColor = colors.border
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${synthEngine.liveFrequency.collectAsState().value.toInt()} Hz",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Button(
                onClick = {
                    if (isSweepRunning) stopAllAudio() else startFrequencySweep()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSweepRunning) colors.danger else colors.accent,
                    contentColor = if (isSweepRunning) androidx.compose.ui.graphics.Color.White else (if (colors.isDark) android.graphics.Color.BLACK.let { androidx.compose.ui.graphics.Color(it) } else androidx.compose.ui.graphics.Color.White)
                )
            ) {
                Icon(
                    imageVector = if (isSweepRunning) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSweepRunning) strings.btnStopSweep else strings.btnStartSweep,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Acoustic Burn-In Noise
        DiagnosticCard(
            title = strings.diagNoiseTitle,
            description = strings.acousticBurnInDesc,
            icon = Icons.AutoMirrored.Rounded.VolumeUp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DiagPillButton(
                    label = strings.btnWhiteNoise,
                    isSelected = activeNoise == AcousticSynthEngine.NoiseMode.WHITE,
                    onClick = { toggleNoise(AcousticSynthEngine.NoiseMode.WHITE) },
                    modifier = Modifier.weight(1f)
                )

                DiagPillButton(
                    label = strings.btnPinkNoise,
                    isSelected = activeNoise == AcousticSynthEngine.NoiseMode.PINK,
                    onClick = { toggleNoise(AcousticSynthEngine.NoiseMode.PINK) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Decibel (dB) Sound Level Meter
        DiagnosticCard(
            title = strings.diagMeterTitle,
            description = strings.diagMeterDesc,
            icon = Icons.Rounded.Speed
        ) {
            if (!hasMicPermission) {
                Text(
                    text = strings.micPermissionRequired,
                    fontSize = 11.sp,
                    color = colors.textMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        try {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } catch (_: Throwable) {}
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Mic,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = strings.grantPermission, fontSize = 12.sp, color = colors.accent)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = strings.liveDb, fontSize = 11.sp, color = colors.textMuted)
                        Text(
                            text = "${liveDb.toInt()} dB",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = strings.peakDb, fontSize = 11.sp, color = colors.textMuted)
                        Text(
                            text = "${peakDb.toInt()} dB",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.warning
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (isMeterRecording) soundMeter.stopListening() else soundMeter.startListening()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (isMeterRecording) strings.btnStopMeter else strings.btnStartMeter,
                            fontSize = 11.sp,
                            color = if (isMeterRecording) colors.danger else colors.accent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Before vs After Comparison Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DiagPillButton(
                        label = if (beforeDb != null) strings.recordBeforeFormat.format(beforeDb?.toInt()) else strings.recordBefore,
                        isSelected = beforeDb != null,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            beforeDb = liveDb
                        },
                        modifier = Modifier.weight(1f)
                    )

                    DiagPillButton(
                        label = if (afterDb != null) strings.recordAfterFormat.format(afterDb?.toInt()) else strings.recordAfter,
                        isSelected = afterDb != null,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            afterDb = liveDb
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (beforeDb != null && afterDb != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val improvement = (afterDb ?: 0f) - (beforeDb ?: 0f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.success.copy(alpha = 0.15f))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.outputImprovedBy.format(improvement),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.success
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Small Native Ad at bottom
        SmallNativeAdView()

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DiagnosticCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    val colors = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = description,
            fontSize = 11.sp,
            color = colors.textMuted,
            lineHeight = 15.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        content()
    }
}

@Composable
private fun DiagPillButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) colors.accent.copy(alpha = 0.20f) else colors.background,
        animationSpec = tween(200),
        label = "pill_bg"
    )

    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(
                if (isSelected) 1.5.dp else 1.dp,
                if (isSelected) colors.accent else colors.border.copy(alpha = 0.5f),
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) colors.accent else colors.textSecondary,
            maxLines = 1
        )
    }
}
