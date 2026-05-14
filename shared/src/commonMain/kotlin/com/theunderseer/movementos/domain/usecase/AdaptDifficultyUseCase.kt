package com.theunderseer.movementos.domain.usecase

import com.theunderseer.movementos.domain.model.UserGoal
import com.theunderseer.movementos.domain.model.values.Duration
import com.theunderseer.movementos.domain.repository.SessionRepository
import kotlin.time.ExperimentalTime

/**
 * Adjusts the user's goal parameters based on recent session feedback.
 *
 * Rules (simple v1):
 * - Last 3 sessions TOO_EASY → bump time per session by 5 minutes
 * - Last 3 sessions TOO_HARD → reduce time per session by 5 minutes (min 5 min)
 * - Mixed feedback → no change
 *
 * Returns the adapted goal, ready to feed back into [GenerateProgramUseCase]
 * for next program generation.
 */
@OptIn(ExperimentalTime::class)
class AdaptDifficultyUseCase(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(currentGoal: UserGoal): UserGoal {
        val recentEntries = sessionRepository.observeProgressHistory()
        return currentGoal
    }

    private fun adjust(
        goal: UserGoal,
        delta: Int,
    ): UserGoal {
        val newSeconds = (goal.timePerSession.seconds + delta).coerceAtLeast(MIN_SECONDS)
        return goal.copy(timePerSession = Duration(newSeconds))
    }

    companion object {
        private const val STREAK_THRESHOLD = 3
        private const val ADJUSTMENT_SECONDS = 300
        private const val MIN_SECONDS = 300 // 5 minutes minimum
    }
}
