package com.shuttletechnologies.speakercleaner.data

enum class WaveformType(val displayName: String, val description: String) {
    SINE("Sine", "Smooth acoustic resonance for fluid expulsion"),
    TRIANGLE("Triangle", "Balanced harmonics for mixed particulate cleaning"),
    SQUARE("Square", "Max acoustic pressure & cone excursion"),
    SAWTOOTH("Sawtooth", "Sharp mechanical dislodgement for dry dust")
}
