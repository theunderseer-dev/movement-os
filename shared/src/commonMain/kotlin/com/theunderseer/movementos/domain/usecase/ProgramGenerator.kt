package com.theunderseer.movementos.domain.usecase

import com.theunderseer.movementos.domain.model.Program
import com.theunderseer.movementos.domain.model.UserGoal

/**
 * Strategy interface for producing a program from a goal.
 *
 * Implemented separately for AI-driven generation (LLM) and deterministic
 * fallback (rule-based). Use case stays the same; strategy swaps.
 */
fun interface ProgramGenerator {
    suspend fun generate(goal: UserGoal): Program
}
