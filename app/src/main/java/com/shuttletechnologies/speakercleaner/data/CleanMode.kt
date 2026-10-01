package com.shuttletechnologies.speakercleaner.data

import com.shuttletechnologies.speakercleaner.localization.StringResources

enum class CleanMode(val defaultDurationSec: Int) {
    WATER_EJECT(60),
    DUST_BLAST(45),
    QUICK_BLAST(30),
    DEEP_CLEAN(120),
    ULTRASONIC(40),
    MANUAL_TONE(0);

    fun getLocalizedName(strings: StringResources): String = when (this) {
        WATER_EJECT -> strings.modeWaterEject
        DUST_BLAST -> strings.modeDustBlast
        QUICK_BLAST -> strings.modeQuickBlast
        DEEP_CLEAN -> strings.modeDeepClean
        ULTRASONIC -> strings.modeUltrasonic
        MANUAL_TONE -> strings.tabGenerator
    }
}
