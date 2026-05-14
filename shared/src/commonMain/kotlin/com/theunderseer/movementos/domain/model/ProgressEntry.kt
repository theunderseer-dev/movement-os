package com.theunderseer.movementos.domain.model

import com.theunderseer.movementos.domain.model.values.DifficultyLevel
import com.theunderseer.movementos.domain.model.values.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * A user's record of a completed session.
 *
 * Used by [AdaptDifficultyUseCase] to inform next-session intensity.
 *
 * @property id stable identifier
 * @property sessionId the session that was performed
 * @property completedAt when the session finished
 * @property actualDuration how long the user actually spent (may differ from estimate)
 * @property perceivedDifficulty user's subjective rating
 * @property notes optional free-text notes
 */
@OptIn(ExperimentalTime::class)
data class ProgressEntry(
    val id: String,
    val sessionId: String,
    val completedAt: Instant,
    val actualDuration: Duration,
    val perceivedDifficulty: DifficultyLevel,
    val notes: String? = null,
)
