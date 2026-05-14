package com.theunderseer.movementos.domain.model

import com.theunderseer.movementos.domain.model.values.Duration

/**
 * A single training session within a program.
 *
 * Sessions are ordered within a program but performed independently.
 *
 * @property id stable identifier
 * @property programId the program this session belongs to
 * @property orderIndex 0-based position in the program (0 = first session)
 * @property exercises ordered list of exercises to perform
 * @property estimatedDuration sum of exercise durations + transitions
 */
data class Session(
    val id: String,
    val programId: String,
    val orderIndex: Int,
    val exercises: List<Exercise>,
    val estimatedDuration: Duration,
)
