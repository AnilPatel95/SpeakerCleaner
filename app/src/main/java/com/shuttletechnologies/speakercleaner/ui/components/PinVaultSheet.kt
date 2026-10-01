package com.shuttletechnologies.speakercleaner.ui.components

import android.view.HapticFeedbackConstants
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.fragment.app.FragmentActivity
import com.shuttletechnologies.speakercleaner.data.PreferencesManager
import com.shuttletechnologies.speakercleaner.localization.appStrings
import com.shuttletechnologies.speakercleaner.theme.LocalAppColors

@Composable
fun PinVaultLockScreen(
    prefs: PreferencesManager,
    onUnlocked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val strings = appStrings()
    val view = LocalView.current

    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isBiometricEnabled by prefs.isBiometricEnabled.collectAsState()

    fun triggerBiometrics() {
        val activity = context as? FragmentActivity ?: return
        val executor = ContextCompat.getMainExecutor(context)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    onUnlocked()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        errorCode != BiometricPrompt.ERROR_CANCELED) {
                        errorMessage = errString.toString()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                }
            }
        )

        val biometricManager = BiometricManager.from(context)
        val canStrongOrCred = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS

        val canStrong = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS

        val canWeak = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(strings.biometricPromptTitle)
            .setSubtitle(strings.biometricPromptSubtitle)

        val promptInfo = if (canStrongOrCred) {
            promptInfoBuilder
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()
        } else if (canStrong) {
            promptInfoBuilder
                .setNegativeButtonText(strings.enterPin)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build()
        } else if (canWeak) {
            promptInfoBuilder
                .setNegativeButtonText(strings.enterPin)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .build()
        } else {
            promptInfoBuilder
                .setNegativeButtonText(strings.enterPin)
                .build()
        }

        try {
            prompt.authenticate(promptInfo)
        } catch (_: Exception) {}
    }

    LaunchedEffect(isBiometricEnabled) {
        if (isBiometricEnabled) {
            triggerBiometrics()
        }
    }

    fun handleKeyPress(digit: String) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        errorMessage = null
        if (enteredPin.length < 4) {
            val newPin = enteredPin + digit
            enteredPin = newPin
            if (newPin.length == 4) {
                if (prefs.verifyPin(newPin)) {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    onUnlocked()
                } else {
                    view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                    errorMessage = strings.pinIncorrect
                    enteredPin = ""
                }
            }
        }
    }

    fun handleBackspace() {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.surfaceElevated)
                    .border(1.dp, colors.border, CircleShape)
                    .padding(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = strings.unlockTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = strings.enterPin,
                fontSize = 13.sp,
                color = colors.textMuted
            )

            Spacer(modifier = Modifier.height(24.dp))

            // PIN Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val filled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (filled) colors.accent else colors.surfaceElevated
                            )
                            .border(
                                1.5.dp,
                                if (filled) colors.accent else colors.border,
                                CircleShape
                            )
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    fontSize = 12.sp,
                    color = colors.danger
                )
            }

            // Biometric quick pill
            if (isBiometricEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.accent.copy(alpha = 0.12f))
                        .clickable { triggerBiometrics() }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Fingerprint,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.tapForBiometric,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Keypad
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("BIO", "0", "DEL")
            )

            for (row in rows) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    for (item in row) {
                        when (item) {
                            "BIO" -> {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(if (isBiometricEnabled) colors.surfaceElevated else androidx.compose.ui.graphics.Color.Transparent)
                                        .clickable(enabled = isBiometricEnabled) { triggerBiometrics() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isBiometricEnabled) {
                                        Icon(
                                            imageVector = Icons.Rounded.Fingerprint,
                                            contentDescription = strings.biometricUnlock,
                                            tint = colors.accent,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                            "DEL" -> {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(colors.surfaceElevated)
                                        .clickable { handleBackspace() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                        contentDescription = strings.deleteKey,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(colors.surfaceElevated)
                                        .border(1.dp, colors.border.copy(alpha = 0.5f), CircleShape)
                                        .clickable { handleKeyPress(item) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
