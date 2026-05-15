package com.theunderseer.movementos.domain.usecase

import com.theunderseer.movementos.domain.model.UserGoal
import com.theunderseer.movementos.domain.model.values.DifficultyLevel
import com.theunderseer.movementos.domain.model.values.Duration
import com.theunderseer.movementos.domain.repository.SessionRepository
import kotlinx.coroutines.flow.first
import kotlin.time.ExperimentalTime

/**
 * Adjusts a user's goal parameters based on perceived difficulty of recent sessions.
 *
 * Strategy (v1, rule-based):
 * - Last [STREAK_THRESHOLD] sessions consistently TOO_EASY → increase time per session
 * - Last [STREAK_THRESHOLD] sessions consistently TOO_HARD → decrease time per session
 * - Mixed feedback or insufficient history → no change
 *
 * The returned goal is intended to feed back into [GenerateProgramUseCase] to produce
 * an appropriately calibrated next program.
 */
@OptIn(ExperimentalTime::class)
class AdaptDifficultyUseCase(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(currentGoal: UserGoal): UserGoal {
        val recent =
            sessionRepository
                .observeProgressHistory()
                .first()
                .take(STREAK_THRESHOLD)

        if (recent.size < STREAK_THRESHOLD) return currentGoal

        val allTooEasy = recent.all { it.perceivedDifficulty == DifficultyLevel.TOO_EASY }
        val allTooHard = recent.all { it.perceivedDifficulty == DifficultyLevel.TOO_HARD }

        return when {
            allTooEasy -> adjust(currentGoal, ADJUSTMENT_SECONDS)
            allTooHard -> adjust(currentGoal, -ADJUSTMENT_SECONDS)
            else -> currentGoal
        }
    }

    private fun adjust(
        goal: UserGoal,
        deltaSeconds: Int,
    ): UserGoal {
        val newSeconds = (goal.timePerSession.seconds + deltaSeconds).coerceAtLeast(MIN_SECONDS)
        return goal.copy(timePerSession = Duration(newSeconds))
    }

    private companion object {
        const val STREAK_THRESHOLD = 3
        const val ADJUSTMENT_SECONDS = 300
        const val MIN_SECONDS = 300
    }
}
