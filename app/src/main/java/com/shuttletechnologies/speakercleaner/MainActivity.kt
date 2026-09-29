package com.shuttletechnologies.speakercleaner

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.fragment.app.FragmentActivity
import com.shuttletechnologies.speakercleaner.audio.AcousticSynthEngine
import com.shuttletechnologies.speakercleaner.audio.AudioOutputRouter
import com.shuttletechnologies.speakercleaner.audio.HapticPulseManager
import com.shuttletechnologies.speakercleaner.audio.SoundLevelMeter
import com.shuttletechnologies.speakercleaner.data.PreferencesManager
import com.shuttletechnologies.speakercleaner.localization.AppStrings
import com.shuttletechnologies.speakercleaner.localization.LocalAppStrings
import com.shuttletechnologies.speakercleaner.theme.LocalAppColors
import com.shuttletechnologies.speakercleaner.theme.SpeakerCleanerTheme
import com.shuttletechnologies.speakercleaner.ui.components.AppBottomNavBar
import com.shuttletechnologies.speakercleaner.ui.components.AppTopBar
import com.shuttletechnologies.speakercleaner.ui.components.ExitRateDialog
import com.shuttletechnologies.speakercleaner.ui.components.PinVaultLockScreen
import com.shuttletechnologies.speakercleaner.ui.screens.DiagnosticsScreen
import com.shuttletechnologies.speakercleaner.ui.screens.HistoryScreen
import com.shuttletechnologies.speakercleaner.ui.screens.ManualToneScreen
import com.shuttletechnologies.speakercleaner.ui.screens.SettingsScreen
import com.shuttletechnologies.speakercleaner.ui.screens.WaterEjectScreen

class MainActivity : FragmentActivity() {

    private lateinit var prefs: PreferencesManager
    private lateinit var synthEngine: AcousticSynthEngine
    private lateinit var audioRouter: AudioOutputRouter
    private lateinit var hapticManager: HapticPulseManager
    private lateinit var soundMeter: SoundLevelMeter

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // API 36 Edge-to-Edge initialization
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        prefs = PreferencesManager(this)
        synthEngine = AcousticSynthEngine()
        audioRouter = AudioOutputRouter(this)
        hapticManager = HapticPulseManager(this)
        soundMeter = SoundLevelMeter(this)

        setContent {
            val themeMode by prefs.themeMode.collectAsState()
            val languageCode by prefs.languageCode.collectAsState()
            val isSecurityEnabled by prefs.isSecurityEnabled.collectAsState()

            var isUnlocked by remember { mutableStateOf(!isSecurityEnabled) }
            var currentTab by remember { mutableIntStateOf(0) }
            var showExitDialog by remember { mutableStateOf(false) }
            var isAudioActive by remember { mutableStateOf(false) }

            val strings = remember(languageCode) { AppStrings.get(languageCode) }

            val topAppBarState = rememberTopAppBarState()
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)

            val isBottomBarVisible by remember {
                derivedStateOf {
                    !isAudioActive && scrollBehavior.state.heightOffset >= -5f
                }
            }

            SpeakerCleanerTheme(themeMode = themeMode) {
                CompositionLocalProvider(LocalAppStrings provides strings) {
                    val colors = LocalAppColors.current

                    if (isSecurityEnabled && !isUnlocked) {
                        PinVaultLockScreen(
                            prefs = prefs,
                            onUnlocked = { isUnlocked = true }
                        )
                    } else {
                        // Back Navigation Handling
                        BackHandler {
                            if (currentTab != 0) {
                                currentTab = 0
                                scrollBehavior.state.heightOffset = 0f
                            } else {
                                showExitDialog = true
                            }
                        }

                        Scaffold(
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(scrollBehavior.nestedScrollConnection),
                            containerColor = colors.background,
                            contentWindowInsets = WindowInsets(0, 0, 0, 0),
                            topBar = {
                                AppTopBar(
                                    currentTab = currentTab,
                                    scrollBehavior = scrollBehavior,
                                    isCleaningActive = isAudioActive
                                )
                            },
                            bottomBar = {
                                AppBottomNavBar(
                                    selectedTab = currentTab,
                                    onTabSelected = { tab ->
                                        currentTab = tab
                                        scrollBehavior.state.heightOffset = 0f
                                    },
                                    isVisible = isBottomBarVisible
                                )
                            }
                        ) { innerPadding ->
                            val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                            val contentBottomPadding = if (isBottomBarVisible) innerPadding.calculateBottomPadding() else navBarInset

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(colors.background)
                                    .padding(
                                        top = innerPadding.calculateTopPadding(),
                                        bottom = contentBottomPadding
                                    )
                            ) {
                                Crossfade(
                                    targetState = currentTab,
                                    label = "tab_crossfade"
                                ) { tab ->
                                    when (tab) {
                                        0 -> WaterEjectScreen(
                                            synthEngine = synthEngine,
                                            audioRouter = audioRouter,
                                            hapticManager = hapticManager,
                                            prefs = prefs,
                                            onActiveStateChanged = { isAudioActive = it }
                                        )
                                        1 -> ManualToneScreen(
                                            synthEngine = synthEngine,
                                            audioRouter = audioRouter,
                                            hapticManager = hapticManager,
                                            onActiveStateChanged = { isAudioActive = it }
                                        )
                                        2 -> DiagnosticsScreen(
                                            synthEngine = synthEngine,
                                            audioRouter = audioRouter,
                                            hapticManager = hapticManager,
                                            soundMeter = soundMeter,
                                            onActiveStateChanged = { isAudioActive = it }
                                        )
                                        3 -> HistoryScreen(
                                            prefs = prefs
                                        )
                                        4 -> SettingsScreen(
                                            prefs = prefs
                                        )
                                    }
                                }
                            }
                        }

                        if (showExitDialog) {
                            ExitRateDialog(
                                onDismissRequest = { showExitDialog = false },
                                onRateClicked = {
                                    prefs.setHasRated(true)
                                    showExitDialog = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        synthEngine.release()
        soundMeter.stopListening()
        hapticManager.stopVibration()
        audioRouter.restoreOriginalRouting()
    }
}
