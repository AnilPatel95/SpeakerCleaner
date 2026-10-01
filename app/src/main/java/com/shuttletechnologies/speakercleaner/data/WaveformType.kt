package com.shuttletechnologies.speakercleaner.data

import com.shuttletechnologies.speakercleaner.localization.StringResources

enum class WaveformType {
    SINE,
    TRIANGLE,
    SQUARE,
    SAWTOOTH;

    fun getLocalizedName(strings: StringResources): String = when (this) {
        SINE -> strings.sineWaveName
        TRIANGLE -> strings.triangleWaveName
        SQUARE -> strings.squareWaveName
        SAWTOOTH -> strings.sawtoothWaveName
    }

    fun getLocalizedDesc(strings: StringResources): String = when (this) {
        SINE -> strings.sineWaveDesc
        TRIANGLE -> strings.triangleWaveDesc
        SQUARE -> strings.squareWaveDesc
        SAWTOOTH -> strings.sawtoothWaveDesc
    }
}
