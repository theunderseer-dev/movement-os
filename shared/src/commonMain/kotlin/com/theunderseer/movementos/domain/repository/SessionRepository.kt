package com.theunderseer.movementos.domain.repository

import com.theunderseer.movementos.domain.model.ProgressEntry
import com.theunderseer.movementos.domain.model.Session
import kotlinx.coroutines.flow.Flow

/**
 * Manages sessions and recorded progress.
 *
 * Sessions themselves are owned by [ProgramRepository] (immutable, part of program).
 * This repository handles user-facing session interactions: recording completion,
 * tracking history, computing streaks.
 */
interface SessionRepository {
    /**
     * Records that the user completed a session.
     *
     * Idempotent — recording the same session twice creates two entries
     * (user may repeat sessions).
     */
    suspend fun recordCompletion(entry: ProgressEntry)

    /**
     * Stream of all progress entries, newest first.
     */
    fun observeProgressHistory(): Flow<List<ProgressEntry>>

    /**
     * One-time fetch of progress for a specific session.
     *
     * @return list of all completions of this session (may be empty)
     */
    suspend fun getProgressForSession(sessionId: String): List<ProgressEntry>

    /**
     * Find the next session the user should perform from a program.
     *
     * Logic: first session that has fewer completions than later sessions,
     * or first session if none completed.
     */
    suspend fun getNextSession(programSessions: List<Session>): Session?
}
