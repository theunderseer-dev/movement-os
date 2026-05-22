package com.theunderseer.movementos.core.testing.fixtures

import com.theunderseer.movementos.domain.model.ProgressEntry
import com.theunderseer.movementos.domain.model.values.DifficultyLevel
import com.theunderseer.movementos.domain.model.values.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
object TestProgress {
    val FIXED_COMPLETED_AT: Instant = Instant.fromEpochMilliseconds(1_700_000_100_000)

    fun aProgressEntry(
        id: String = "entry-1",
        sessionId: String = "session-1",
        completedAt: Instant = FIXED_COMPLETED_AT,
        actualDuration: Duration = Duration.ofMinutes(22),
        perceivedDifficulty: DifficultyLevel = DifficultyLevel.JUST_RIGHT,
        notes: String? = null,
    ): ProgressEntry =
        ProgressEntry(
            id = id,
            sessionId = sessionId,
            completedAt = completedAt,
            actualDuration = actualDuration,
            perceivedDifficulty = perceivedDifficulty,
            notes = notes,
        )
}
