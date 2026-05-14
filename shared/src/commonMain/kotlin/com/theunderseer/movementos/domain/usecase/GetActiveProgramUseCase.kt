package com.theunderseer.movementos.domain.usecase

import com.theunderseer.movementos.domain.model.Program
import com.theunderseer.movementos.domain.repository.ProgramRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observes the user's currently active program.
 *
 * Returns null when no program is generated — UI shows onboarding/generation flow.
 */
class GetActiveProgramUseCase(
    private val programRepository: ProgramRepository,
) {
    operator fun invoke(): Flow<Program?> = programRepository.observeActiveProgram()
}
