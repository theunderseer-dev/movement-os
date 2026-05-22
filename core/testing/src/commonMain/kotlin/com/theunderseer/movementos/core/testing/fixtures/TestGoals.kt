package com.theunderseer.movementos.core.testing.fixtures

import com.theunderseer.movementos.domain.model.UserGoal
import com.theunderseer.movementos.domain.model.values.Duration
import com.theunderseer.movementos.domain.model.values.MovementType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
object TestGoals {
    val FIXED_CREATED_AT: Instant = Instant.fromEpochMilliseconds(1_700_000_000_000)

    fun aGoal(
        id: String = "goal-1",
        description: String = "Improve mobility",
        focus: MovementType = MovementType.MOBILITY,
        sessionsPerWeek: Int = 3,
        timePerSession: Duration = Duration.ofMinutes(20),
        createdAt: Instant = FIXED_CREATED_AT,
    ): UserGoal =
        UserGoal(
            id = id,
            description = description,
            focus = focus,
            sessionsPerWeek = sessionsPerWeek,
            timePerSession = timePerSession,
            createdAt = createdAt,
        )
}
