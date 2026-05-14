package com.theunderseer.movementos.domain.model

import com.theunderseer.movementos.domain.model.values.Duration
import com.theunderseer.movementos.domain.model.values.MovementType

/**
 * A single movement performed during a session.
 *
 * @property id stable identifier (deterministic from name + parameters when generated)
 * @property name human-readable name shown in UI ("Child's pose", "Cat-cow")
 * @property duration how long to hold or perform
 * @property type primary movement category
 * @property cues optional verbal cues read by TTS during the session
 */
data class Exercise(
    val id: String,
    val name: String,
    val duration: Duration,
    val type: MovementType,
    val cues: List<String> = emptyList(),
)
