package com.theunderseer.movementos.domain.usecase

import com.theunderseer.movementos.domain.model.ProgressEntry
import com.theunderseer.movementos.domain.model.Session
import com.theunderseer.movementos.domain.model.values.DifficultyLevel
import com.theunderseer.movementos.domain.model.values.Duration
import com.theunderseer.movementos.domain.repository.SessionRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Records a completed session and returns the saved entry.
 *
 * Generates entry id deterministically from session + timestamp, so the same
 * completion isn't recorded twice if the user double-taps "finish".
 */
@OptIn(ExperimentalTime::class)
class RecordSessionUseCase(
    private val sessionRepository: SessionRepository,
    private val clock: Clock = Clock.System,
) {
    suspend operator fun invoke(
        session: Session,
        actualDuration: Duration,
        difficulty: DifficultyLevel,
        notes: String? = null,
    ): ProgressEntry {
        val completedAt: Instant = clock.now()
        val entry =
            ProgressEntry(
                id = "${session.id}-${completedAt.toEpochMilliseconds()}",
                sessionId = session.id,
                completedAt = completedAt,
                actualDuration = actualDuration,
                perceivedDifficulty = difficulty,
                notes = notes?.takeIf { it.isNotBlank() },
            )
        sessionRepository.recordCompletion(entry)
        return entry
    }
}
