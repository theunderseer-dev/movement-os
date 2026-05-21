package com.theunderseer.movementos.core.testing.fixtures

import com.theunderseer.movementos.domain.model.Program
import com.theunderseer.movementos.domain.model.Session
import com.theunderseer.movementos.domain.model.values.MovementType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Test fixtures for [Program] entities.
 *
 * Use [aProgram] for minimal-config defaults, override fields as needed:
 * ```
 * val program = aProgram(name = "Custom", isActive = true)
 * ```
 *
 * Avoids per-test boilerplate while keeping test intent visible (only mention
 * fields the test cares about).
 */
@OptIn(ExperimentalTime::class)
object TestPrograms {
    val FIXED_GENERATED_AT: Instant = Instant.fromEpochMilliseconds(1_700_000_000_000)

    fun aProgram(
        id: String = "program-1",
        goalId: String = "goal-1",
        name: String = "Back relief program",
        description: String = "10-day plan",
        primaryType: MovementType = MovementType.BACK_PAIN_RELIEF,
        sessions: List<Session> = emptyList(),
        generatedAt: Instant = FIXED_GENERATED_AT,
        isActive: Boolean = true,
    ): Program =
        Program(
            id = id,
            goalId = goalId,
            name = name,
            description = description,
            primaryType = primaryType,
            sessions = sessions,
            generatedAt = generatedAt,
            isActive = isActive,
        )
}
