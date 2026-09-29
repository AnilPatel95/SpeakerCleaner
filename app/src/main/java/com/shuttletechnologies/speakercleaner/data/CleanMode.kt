package com.shuttletechnologies.speakercleaner.data

enum class CleanMode(val defaultDurationSec: Int) {
    WATER_EJECT(60),
    DUST_BLAST(45),
    QUICK_BLAST(30),
    DEEP_CLEAN(120),
    MANUAL_TONE(0)
}
