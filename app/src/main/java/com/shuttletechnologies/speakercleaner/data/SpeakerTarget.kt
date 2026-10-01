package com.shuttletechnologies.speakercleaner.data

import com.shuttletechnologies.speakercleaner.localization.StringResources

enum class SpeakerTarget {
    LOUDSPEAKER,
    EARPIECE;

    fun getLocalizedName(strings: StringResources): String = when (this) {
        LOUDSPEAKER -> strings.targetLoudspeakerFull
        EARPIECE -> strings.targetEarpieceFull
    }
}
