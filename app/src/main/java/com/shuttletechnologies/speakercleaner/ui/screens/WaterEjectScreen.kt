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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shuttletechnologies.speakercleaner.audio.AcousticSynthEngine
import com.shuttletechnologies.speakercleaner.audio.AudioOutputRouter
import com.shuttletechnologies.speakercleaner.audio.HapticPulseManager
import com.shuttletechnologies.speakercleaner.audio.VolumeController
import com.shuttletechnologies.speakercleaner.sensors.TiltGravitySensor
import com.shuttletechnologies.speakercleaner.data.CleanMode
import com.shuttletechnologies.speakercleaner.data.CleaningSession
import com.shuttletechnologies.speakercleaner.data.PreferencesManager
import com.shuttletechnologies.speakercleaner.data.SpeakerTarget
import com.shuttletechnologies.speakercleaner.data.WaveformType
import com.shuttletechnologies.speakercleaner.localization.appStrings
import com.shuttletechnologies.speakercleaner.theme.LocalAppColors
import com.shuttletechnologies.speakercleaner.ui.components.AcousticGauge
import com.shuttletechnologies.speakercleaner.ui.components.SmallNativeAdView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WaterEjectScreen(
    synthEngine: AcousticSynthEngine,
    audioRouter: AudioOutputRouter,
    hapticManager: HapticPulseManager,
    prefs: PreferencesManager,
    onActiveStateChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = appStrings()
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val context = LocalContext.current
    val volumeController = remember { VolumeController(context) }
    val tiltSensor = remember { TiltGravitySensor(context) }
    val tiltState by tiltSensor.tiltState.collectAsState()

    var selectedTarget by remember { mutableStateOf(SpeakerTarget.LOUDSPEAKER) }
    var selectedMode by remember { mutableStateOf(CleanMode.WATER_EJECT) }
    var isCleaning by remember { mutableStateOf(false) }

    var currentProgress by remember { mutableFloatStateOf(0f) }
    var currentFrequency by remember { mutableFloatStateOf(165f) }
    var phaseText by remember { mutableStateOf(strings.statusIdle) }
    var remainingSeconds by remember { mutableIntStateOf(selectedMode.defaultDurationSec) }

    DisposableEffect(Unit) {
        tiltSensor.startListening()
        onDispose {
            tiltSensor.stopListening()
            volumeController.restoreOriginalVolume(isVoiceCall = selectedTarget == SpeakerTarget.EARPIECE)
        }
    }

    LaunchedEffect(isCleaning) {
        onActiveStateChanged(isCleaning)
    }

    fun stopCleaning(completed: Boolean = false) {
        isCleaning = false
        synthEngine.stopPlayback()
        hapticManager.stopVibration()
        audioRouter.restoreOriginalRouting()
        volumeController.restoreOriginalVolume(isVoiceCall = selectedTarget == SpeakerTarget.EARPIECE)

        if (completed) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            phaseText = strings.statusCompleted
            prefs.recordSession(
                CleaningSession(
                    mode = selectedMode,
                    target = selectedTarget,
                    durationSeconds = selectedMode.defaultDurationSec,
                    completed = true
                )
            )
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
            phaseText = strings.statusIdle
        }
    }

    fun startCleaningCycle() {
        if (isCleaning) return
        isCleaning = true
        currentProgress = 0f
        remainingSeconds = selectedMode.defaultDurationSec

        // Audio and vibration setup
        audioRouter.routeTo(selectedTarget)
        audioRouter.setMaxVolume()

        val useVoiceCall = selectedTarget == SpeakerTarget.EARPIECE
        volumeController.maximizeVolumeForCleaning(isVoiceCall = useVoiceCall)

        synthEngine.volumeLevel = 1.0f
        synthEngine.stereoPan = 0.0f
        synthEngine.noiseMode = AcousticSynthEngine.NoiseMode.NONE
        synthEngine.startPlayback(useVoiceCallStream = useVoiceCall)

        if (selectedMode == CleanMode.DUST_BLAST) {
            hapticManager.startDustBlastVibration()
            synthEngine.waveform = WaveformType.SQUARE
        } else {
            hapticManager.startWaterEjectVibration()
            synthEngine.waveform = WaveformType.SINE
        }

        scope.launch {
            val totalSec = selectedMode.defaultDurationSec
            val updateIntervalMs = 100L
            val totalSteps = (totalSec * 1000L) / updateIntervalMs
            var step = 0

            while (step < totalSteps && isCleaning) {
                delay(updateIntervalMs)
                step++
                val progress = step.toFloat() / totalSteps.toFloat()
                currentProgress = progress
                remainingSeconds = ((totalSteps - step) * updateIntervalMs / 1000L).toInt()

                // Acoustic Frequency Progression Algorithm
                when (selectedMode) {
                    CleanMode.WATER_EJECT, CleanMode.DEEP_CLEAN -> {
                        when {
                            progress < 0.35f -> {
                                phaseText = strings.phase1
                                synthEngine.waveform = WaveformType.SINE
                                // Resonance sweep around 140 - 165 Hz
                                val sweep = 140f + (25f * (progress / 0.35f))
                                currentFrequency = sweep
                                synthEngine.targetFrequency = sweep
                            }
                            progress < 0.75f -> {
                                phaseText = strings.phase2
                                synthEngine.waveform = WaveformType.TRIANGLE
                                // Air displacement stepping
                                val subProg = (progress - 0.35f) / 0.40f
                                val steppedFreq = 165f + (subProg * 350f)
                                currentFrequency = steppedFreq
                                synthEngine.targetFrequency = steppedFreq
                            }
                            else -> {
                                phaseText = strings.phase3
                                synthEngine.waveform = WaveformType.SAWTOOTH
                                // High frequency vaporization pulse 1200 - 3200 Hz
                                val subProg = (progress - 0.75f) / 0.25f
                                val highFreq = 1200f + (subProg * 2000f)
                                currentFrequency = highFreq
                                synthEngine.targetFrequency = highFreq
                            }
                        }
                    }
                    CleanMode.DUST_BLAST -> {
                        phaseText = strings.phaseUltrasonicAgitation
                        synthEngine.waveform = WaveformType.SQUARE
                        // Rapid frequency oscillation 300 - 2400 Hz
                        val cycle = (step % 20).toFloat() / 20f
                        val freq = 300f + (cycle * 2100f)
                        currentFrequency = freq
                        synthEngine.targetFrequency = freq
                    }
                    CleanMode.QUICK_BLAST -> {
                        phaseText = strings.phaseRapidAirPulse
                        synthEngine.waveform = WaveformType.SINE
                        val freq = 165f + ((step % 10) * 15f)
                        currentFrequency = freq
                        synthEngine.targetFrequency = freq
                    }
                    CleanMode.ULTRASONIC -> {
                        phaseText = strings.phaseSilentUltrasonic
                        synthEngine.waveform = WaveformType.SINE
                        val cycle = (step % 20).toFloat() / 20f
                        val freq = 18500f + (cycle * 3000f)
                        currentFrequency = freq
                        synthEngine.targetFrequency = freq
                    }
                    else -> {}
                }
            }

            if (isCleaning) {
                stopCleaning(completed = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Speaker Target Selector (Two-Tier Modern Chips)
        Text(
            text = strings.speakerTargetTitle,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textMuted,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SpeakerChip(
                title = strings.targetLoudspeaker,
                icon = Icons.AutoMirrored.Rounded.VolumeUp,
                isSelected = selectedTarget == SpeakerTarget.LOUDSPEAKER,
                onClick = {
                    if (!isCleaning) {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        selectedTarget = SpeakerTarget.LOUDSPEAKER
                    }
                },
                modifier = Modifier.weight(1f)
            )

            SpeakerChip(
                title = strings.targetEarpiece,
                icon = Icons.Rounded.Hearing,
                isSelected = selectedTarget == SpeakerTarget.EARPIECE,
                onClick = {
                    if (!isCleaning) {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        selectedTarget = SpeakerTarget.EARPIECE
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cleaning Mode Selector
        Text(
            text = strings.cleaningMode,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textMuted,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeChip(
                    title = strings.modeWaterEject,
                    duration = "60s",
                    icon = Icons.Rounded.WaterDrop,
                    isSelected = selectedMode == CleanMode.WATER_EJECT,
                    onClick = {
                        if (!isCleaning) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            selectedMode = CleanMode.WATER_EJECT
                            remainingSeconds = 60
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                ModeChip(
                    title = strings.modeDustBlast,
                    duration = "45s",
                    icon = Icons.Rounded.Air,
                    isSelected = selectedMode == CleanMode.DUST_BLAST,
                    onClick = {
                        if (!isCleaning) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            selectedMode = CleanMode.DUST_BLAST
                            remainingSeconds = 45
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                ModeChip(
                    title = strings.modeDeepClean,
                    duration = "120s",
                    icon = Icons.Rounded.CleaningServices,
                    isSelected = selectedMode == CleanMode.DEEP_CLEAN,
                    onClick = {
                        if (!isCleaning) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            selectedMode = CleanMode.DEEP_CLEAN
                            remainingSeconds = 120
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeChip(
                    title = strings.modeQuickBlast,
                    duration = "30s",
                    icon = Icons.Rounded.Bolt,
                    isSelected = selectedMode == CleanMode.QUICK_BLAST,
                    onClick = {
                        if (!isCleaning) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            selectedMode = CleanMode.QUICK_BLAST
                            remainingSeconds = 30
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                ModeChip(
                    title = strings.modeUltrasonic,
                    duration = "40s",
                    icon = Icons.Rounded.Waves,
                    isSelected = selectedMode == CleanMode.ULTRASONIC,
                    onClick = {
                        if (!isCleaning) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            selectedMode = CleanMode.ULTRASONIC
                            remainingSeconds = 40
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Circular Acoustic Water Ejection Gauge
        AcousticGauge(
            progress = currentProgress,
            frequency = currentFrequency,
            isCleaning = isCleaning,
            phaseText = phaseText,
            size = 220.dp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Timer Remaining Pill
        if (isCleaning) {
            Text(
                text = "${strings.remainingSec}: ${remainingSeconds}s",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.accent
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Interactive Gravity Tilt & Orientation Guide
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (tiltState.isOptimalAngle) colors.success.copy(alpha = 0.16f)
                    else colors.surfaceElevated
                )
                .border(
                    width = 1.dp,
                    color = if (tiltState.isOptimalAngle) colors.success.copy(alpha = 0.6f) else colors.border.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (tiltState.isOptimalAngle) Icons.Rounded.CheckCircle else Icons.Rounded.ScreenRotation,
                    contentDescription = null,
                    tint = if (tiltState.isOptimalAngle) colors.success else colors.accent,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (tiltState.isOptimalAngle) {
                        strings.gravityAssistOptimal.format(tiltState.pitchDeg.toInt())
                    } else {
                        strings.gravityGuideInstruction
                    },
                    fontSize = 11.sp,
                    fontWeight = if (tiltState.isOptimalAngle) FontWeight.Bold else FontWeight.Medium,
                    color = if (tiltState.isOptimalAngle) colors.success else colors.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Primary Start / Stop CTA Button
        Button(
            onClick = {
                if (isCleaning) {
                    stopCleaning(completed = false)
                } else {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    startCleaningCycle()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isCleaning) colors.danger else colors.accent,
                contentColor = if (isCleaning) androidx.compose.ui.graphics.Color.White else (if (colors.isDark) android.graphics.Color.BLACK.let { androidx.compose.ui.graphics.Color(it) } else androidx.compose.ui.graphics.Color.White)
            )
        ) {
            Icon(
                imageVector = if (isCleaning) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isCleaning) strings.btnStopCleaning else strings.btnStartCleaning,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Instructions Card
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
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = strings.tipHeader,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            InstructionItem(
                icon = Icons.AutoMirrored.Rounded.VolumeUp,
                text = strings.tip1
            )
            InstructionItem(
                icon = Icons.Rounded.PhoneAndroid,
                text = strings.tip2
            )
            InstructionItem(
                icon = Icons.Rounded.Headphones,
                text = strings.tip3
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Small Native Ad at Bottom
        SmallNativeAdView()

        // Clearance spacer
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SpeakerChip(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) colors.accent.copy(alpha = 0.18f) else colors.surfaceElevated,
        animationSpec = tween(220),
        label = "chip_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) colors.accent else colors.border.copy(alpha = 0.5f),
        animationSpec = tween(220),
        label = "chip_border"
    )

    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) colors.accent else colors.textMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) colors.accent else colors.textPrimary
        )
    }
}

@Composable
private fun ModeChip(
    title: String,
    duration: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) colors.accent.copy(alpha = 0.18f) else colors.surfaceElevated,
        animationSpec = tween(220),
        label = "mode_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) colors.accent else colors.border.copy(alpha = 0.5f),
        animationSpec = tween(220),
        label = "mode_border"
    )

    Column(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) colors.accent else colors.textMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) colors.accent else colors.textPrimary,
            maxLines = 1
        )
        Text(
            text = duration,
            fontSize = 8.sp,
            fontWeight = FontWeight.Normal,
            color = colors.textMuted
        )
    }
}

@Composable
private fun InstructionItem(
    icon: ImageVector,
    text: String
) {
    val colors = LocalAppColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.accentSecondary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            color = colors.textSecondary,
            lineHeight = 15.sp
        )
    }
}
