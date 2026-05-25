package com.theunderseer.movementos.domain.usecase

import com.theunderseer.movementos.domain.model.Program
import com.theunderseer.movementos.domain.model.UserGoal
import com.theunderseer.movementos.domain.repository.ProgramRepository

/**
 * Generates a new program for the user's goal.
 *
 * Delegates the actual generation strategy (AI, rule-based, hybrid) to the
 * [ProgramGenerator] passed in. This keeps the use case agnostic of how programs
 * are produced. Same use case orchestrates AI and fallback paths.
 */
class GenerateProgramUseCase(
    private val programRepository: ProgramRepository,
    private val programGenerator: ProgramGenerator,
    private val fallbackGenerator: ProgramGenerator,
) {
    suspend operator fun invoke(goal: UserGoal): Result<Program> =
        runCatching {
            val program =
                runCatching { programGenerator.generate(goal) }
                    .getOrElse { fallbackGenerator.generate(goal) }
            programRepository.save(program)
            program
        }
}
