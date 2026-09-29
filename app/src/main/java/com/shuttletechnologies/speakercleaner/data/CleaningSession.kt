package com.shuttletechnologies.speakercleaner.data

data class CleaningSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val mode: CleanMode,
    val target: SpeakerTarget,
    val durationSeconds: Int,
    val initialDb: Float? = null,
    val finalDb: Float? = null,
    val completed: Boolean = true
)
