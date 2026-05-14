package com.theunderseer.movementos.domain.model.values

/**
 * Subjective difficulty perceived by the user after a session.
 *
 * Used by [AdaptDifficultyUseCase] to tune the next session's intensity.
 */
enum class DifficultyLevel {
    TOO_EASY,
    JUST_RIGHT,
    TOO_HARD,
}
