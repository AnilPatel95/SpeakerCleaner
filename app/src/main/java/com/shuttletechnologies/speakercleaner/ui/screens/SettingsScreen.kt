package com.shuttletechnologies.speakercleaner.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.HapticFeedbackConstants
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.shuttletechnologies.speakercleaner.data.PreferencesManager
import com.shuttletechnologies.speakercleaner.localization.Locales
import com.shuttletechnologies.speakercleaner.localization.SupportedLanguage
import com.shuttletechnologies.speakercleaner.localization.appStrings
import com.shuttletechnologies.speakercleaner.theme.LocalAppColors
import com.shuttletechnologies.speakercleaner.ui.components.AppLogo
import com.shuttletechnologies.speakercleaner.ui.components.SmallNativeAdView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    prefs: PreferencesManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val strings = appStrings()
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val currentThemeMode by prefs.themeMode.collectAsState()
    val currentLangCode by prefs.languageCode.collectAsState()
    val isSecurityEnabled by prefs.isSecurityEnabled.collectAsState()
    val isBiometricEnabled by prefs.isBiometricEnabled.collectAsState()

    var showLanguageSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Theme Selector Section (4-Tier Modern Chips)
        SectionHeader(title = strings.settingsThemeTitle)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeChip(
                title = strings.themeSystem,
                icon = Icons.Rounded.BrightnessAuto,
                isSelected = currentThemeMode == 0,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    prefs.setThemeMode(0)
                },
                modifier = Modifier.weight(1f)
            )

            ThemeChip(
                title = strings.themeDarkStudio,
                icon = Icons.Rounded.Nightlight,
                isSelected = currentThemeMode == 1,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    prefs.setThemeMode(1)
                },
                modifier = Modifier.weight(1f)
            )

            ThemeChip(
                title = strings.themeAmoled,
                icon = Icons.Rounded.DarkMode,
                isSelected = currentThemeMode == 2,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    prefs.setThemeMode(2)
                },
                modifier = Modifier.weight(1f)
            )

            ThemeChip(
                title = strings.themeLight,
                icon = Icons.Rounded.LightMode,
                isSelected = currentThemeMode == 3,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    prefs.setThemeMode(3)
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Language Selector
        SectionHeader(title = strings.settingsLanguageTitle)

        val activeLang = Locales.getLanguage(currentLangCode)
        ActionCard(
            title = "${activeLang.flagEmoji}  ${activeLang.nameNative} (${activeLang.nameEnglish})",
            subtitle = "Tap to switch language",
            icon = Icons.Rounded.Language,
            trailingIcon = Icons.Rounded.ChevronRight,
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                showLanguageSheet = true
            }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 3. App Security & Privacy
        SectionHeader(title = strings.settingsSecurityTitle)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            // PIN Lock Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.pinLock,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = if (prefs.hasPinConfigured()) "PIN is active" else "Setup a 4-digit PIN",
                        fontSize = 10.sp,
                        color = colors.textMuted
                    )
                }
                Switch(
                    checked = isSecurityEnabled,
                    onCheckedChange = { checked ->
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        if (checked && !prefs.hasPinConfigured()) {
                            showPinDialog = true
                        } else {
                            prefs.setSecurityEnabled(checked)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.accent,
                        checkedTrackColor = colors.accent.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Biometrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Fingerprint,
                    contentDescription = null,
                    tint = colors.accentSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.biometricUnlock,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Unlock with fingerprint or face",
                        fontSize = 10.sp,
                        color = colors.textMuted
                    )
                }
                Switch(
                    checked = isBiometricEnabled,
                    onCheckedChange = { checked ->
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        if (checked && !prefs.hasPinConfigured()) {
                            showPinDialog = true
                        } else {
                            prefs.setBiometricEnabled(checked)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.accentSecondary,
                        checkedTrackColor = colors.accentSecondary.copy(alpha = 0.3f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. About & Community Cards
        SectionHeader(title = strings.settingsAboutTitle)

        ActionCard(
            title = strings.rateUsTitle,
            subtitle = strings.rateUsDesc,
            icon = Icons.Rounded.Star,
            iconTint = colors.warning,
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                launchPlayStore(context)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        ActionCard(
            title = strings.feedbackTitle,
            subtitle = strings.feedbackDesc,
            icon = Icons.Rounded.Email,
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                sendFeedbackEmail(context, strings.copiedFeedback)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        ActionCard(
            title = strings.moreAppsTitle,
            subtitle = "Explore more utilities by Shuttle Technologies",
            icon = Icons.Rounded.Apps,
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                launchDeveloperPage(context)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // App Branding Footer
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AppLogo(size = 52.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = strings.appName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = strings.versionText,
                fontSize = 11.sp,
                color = colors.textMuted
            )
            Text(
                text = "Shuttle Technologies © 2026",
                fontSize = 10.sp,
                color = colors.textMuted.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Small Native Ad at Bottom
        SmallNativeAdView()

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Language Selection Modal Bottom Sheet
    if (showLanguageSheet) {
        var searchQuery by remember { mutableStateOf("") }
        val filteredLanguages = remember(searchQuery) {
            if (searchQuery.isBlank()) Locales.supportedLanguages
            else Locales.supportedLanguages.filter {
                it.nameEnglish.contains(searchQuery, ignoreCase = true) ||
                        it.nameNative.contains(searchQuery, ignoreCase = true)
            }
        }

        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            sheetState = sheetState,
            containerColor = colors.surfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = strings.settingsLanguageTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(text = "Search language...", fontSize = 12.sp, color = colors.textMuted) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.border
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.height(340.dp)) {
                    items(filteredLanguages) { lang ->
                        val isSelected = lang.code.equals(currentLangCode, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) colors.accent.copy(alpha = 0.15f) else androidx.compose.ui.graphics.Color.Transparent)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    prefs.setLanguageCode(lang.code)
                                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                                        showLanguageSheet = false
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = lang.flagEmoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = lang.nameNative,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) colors.accent else colors.textPrimary
                                )
                                Text(
                                    text = lang.nameEnglish,
                                    fontSize = 10.sp,
                                    color = colors.textMuted
                                )
                            }
                            if (isSelected) {
                                Text(
                                    text = "✓",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // PIN Setup Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                pinInput = ""
                confirmPinInput = ""
                pinError = null
            },
            title = {
                Text(
                    text = strings.setupPin,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4) pinInput = it },
                        placeholder = { Text("Enter 4-Digit PIN", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.border
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmPinInput,
                        onValueChange = { if (it.length <= 4) confirmPinInput = it },
                        placeholder = { Text("Confirm 4-Digit PIN", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.border
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (pinError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = pinError!!, fontSize = 11.sp, color = colors.danger)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length != 4) {
                            pinError = "PIN must be exactly 4 digits"
                        } else if (pinInput != confirmPinInput) {
                            pinError = "PINs do not match"
                        } else {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            prefs.setPin(pinInput)
                            prefs.setSecurityEnabled(true)
                            showPinDialog = false
                            pinInput = ""
                            confirmPinInput = ""
                            pinError = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) {
                    Text("Save PIN", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.Black)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPinDialog = false
                        pinInput = ""
                        confirmPinInput = ""
                        pinError = null
                    }
                ) {
                    Text("Cancel", fontSize = 12.sp, color = colors.textMuted)
                }
            },
            containerColor = colors.surfaceElevated
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    val colors = LocalAppColors.current
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = colors.textMuted,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
    )
}

@Composable
private fun ThemeChip(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) colors.accent.copy(alpha = 0.18f) else colors.surfaceElevated,
        animationSpec = tween(200),
        label = "theme_bg"
    )

    Column(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                if (isSelected) 1.5.dp else 1.dp,
                if (isSelected) colors.accent else colors.border.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) colors.accent else colors.textMuted,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = title,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) colors.accent else colors.textPrimary,
            maxLines = 1
        )
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color? = null,
    trailingIcon: ImageVector? = null,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint ?: colors.accent,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = colors.textMuted,
                maxLines = 1
            )
        }

        if (trailingIcon != null) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun launchPlayStore(context: Context) {
    val packageName = context.packageName
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) {
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        )
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(webIntent)
    }
}

private fun sendFeedbackEmail(context: Context, copiedMsg: String) {
    val email = "ved.om9563@gmail.com"
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email?subject=Speaker%20Cleaner%20Feedback")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Support Email", email))
        Toast.makeText(context, copiedMsg, Toast.LENGTH_SHORT).show()
    }
}

private fun launchDeveloperPage(context: Context) {
    try {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/developer?id=shuttletechnologies")
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) {}
}
