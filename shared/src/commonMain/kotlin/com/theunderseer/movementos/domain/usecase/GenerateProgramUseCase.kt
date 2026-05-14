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
) {
    suspend operator fun invoke(goal: UserGoal): Result<Program> =
        runCatching {
            val program = programGenerator.generate(goal)
            programRepository.save(program)
            program
        }
}

/**
 * Strategy interface for producing a program from a goal.
 *
 * Implemented separately for AI-driven generation (LLM) and deterministic
 * fallback (rule-based). Use case stays the same; strategy swaps.
 */
fun interface ProgramGenerator {
    suspend fun generate(goal: UserGoal): Program
}
