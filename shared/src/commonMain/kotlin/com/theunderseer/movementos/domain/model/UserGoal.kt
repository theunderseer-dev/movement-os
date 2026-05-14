package com.theunderseer.movementos.domain.model

import com.theunderseer.movementos.domain.model.values.Duration
import com.theunderseer.movementos.domain.model.values.MovementType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * What the user wants to achieve, drives program generation.
 *
 * @property id stable identifier
 * @property description free-text description ("Touch my toes by August")
 * @property focus primary movement type the goal addresses
 * @property sessionsPerWeek how often the user commits to practicing
 * @property timePerSession typical session length the user can afford
 * @property createdAt when this goal was set
 */
@OptIn(ExperimentalTime::class)
data class UserGoal(
    val id: String,
    val description: String,
    val focus: MovementType,
    val sessionsPerWeek: Int,
    val timePerSession: Duration,
    val createdAt: Instant,
)
