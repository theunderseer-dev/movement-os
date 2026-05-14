package com.theunderseer.movementos.domain.model

import com.theunderseer.movementos.domain.model.values.MovementType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * A multi-session training plan generated for a specific user goal.
 *
 * Programs are immutable once generated. To adjust difficulty, the application
 * generates a new program rather than mutating an existing one.
 *
 * @property id stable identifier
 * @property goalId the goal this program addresses
 * @property name human-readable title
 * @property description short summary shown on the home screen
 * @property primaryType main movement focus
 * @property sessions ordered list of sessions making up the program
 * @property generatedAt when this program was generated
 * @property isActive whether this is the user's current active program
 */
@OptIn(ExperimentalTime::class)
data class Program(
    val id: String,
    val goalId: String,
    val name: String,
    val description: String,
    val primaryType: MovementType,
    val sessions: List<Session>,
    val generatedAt: Instant,
    val isActive: Boolean,
)
