package com.theunderseer.movementos.data.repository

import com.theunderseer.movementos.data.local.ProgressLocalDataSource
import com.theunderseer.movementos.data.local.SessionLocalDataSource
import com.theunderseer.movementos.domain.model.ProgressEntry
import com.theunderseer.movementos.domain.model.Session
import com.theunderseer.movementos.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow

internal class DefaultSessionRepository(
    @Suppress("UnusedPrivateProperty")
    private val sessionDataSource: SessionLocalDataSource,
    private val progressDataSource: ProgressLocalDataSource,
) : SessionRepository {
    override suspend fun recordCompletion(entry: ProgressEntry) = progressDataSource.upsert(entry)

    override fun observeProgressHistory(): Flow<List<ProgressEntry>> = progressDataSource.observeAll()

    override suspend fun getProgressForSession(sessionId: String): List<ProgressEntry> =
        progressDataSource.getBySessionId(
            sessionId,
        )

    override suspend fun getNextSession(programSessions: List<Session>): Session? {
        if (programSessions.isEmpty()) return null
        val sorted = programSessions.sortedBy { it.orderIndex }
        val completionCounts =
            sorted.associateWith { progressDataSource.getBySessionId(it.id).size }
        val minCompletions = completionCounts.values.min()
        return completionCounts.entries.first { it.value == minCompletions }.key
    }
}
