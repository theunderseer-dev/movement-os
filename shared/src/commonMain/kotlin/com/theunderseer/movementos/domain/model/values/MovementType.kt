package com.theunderseer.movementos.domain.model.values

/**
 * Category of movement focus for a program or session.
 *
 * Used both for filtering and for prompt generation in [GenerateProgramUseCase].
 */
enum class MovementType {
    MOBILITY,
    STRENGTH,
    FLEXIBILITY,
    BALANCE,
    RECOVERY,
    BACK_PAIN_RELIEF,
}
