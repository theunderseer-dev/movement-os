package com.theunderseer.movementos.domain.repository

import com.theunderseer.movementos.domain.model.Program
import kotlinx.coroutines.flow.Flow

/**
 * Source of truth for programs — backed by local DB with optional remote sync.
 *
 * Implementations must:
 * - Return Flow for queries that change over time (active program changes when user generates new)
 * - Return suspend for one-shot writes
 * - Hide all infrastructure (SQLDelight, Ktor, cache) — callers only see domain models
 */
interface ProgramRepository {
    /**
     * Stream of the currently active program for the user.
     *
     * Emits null when no program is generated yet.
     */
    fun observeActiveProgram(): Flow<Program?>

    /**
     * One-time fetch of a program by id.
     *
     * @return program if found, null otherwise
     */
    suspend fun getById(id: String): Program?

    /**
     * Persist a newly generated program.
     *
     * Generating a program automatically marks it active and deactivates previous ones.
     */
    suspend fun save(program: Program)

    /**
     * Mark a program inactive without deleting it (for history).
     */
    suspend fun deactivate(id: String)
}
