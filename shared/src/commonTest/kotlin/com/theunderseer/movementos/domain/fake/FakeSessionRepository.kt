package com.theunderseer.movementos.domain.fake

import com.theunderseer.movementos.domain.model.ProgressEntry
import com.theunderseer.movementos.domain.model.Session
import com.theunderseer.movementos.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSessionRepository : SessionRepository {
    private val entries = MutableStateFlow<List<ProgressEntry>>(emptyList())

    override suspend fun recordCompletion(entry: ProgressEntry) {
        entries.value = entries.value + entry
    }

    override fun observeProgressHistory(): Flow<List<ProgressEntry>> = entries

    override suspend fun getProgressForSession(sessionId: String): List<ProgressEntry> = entries.value.filter { it.sessionId == sessionId }

    override suspend fun getNextSession(programSessions: List<Session>): Session? {
        val completedIds = entries.value.map { it.sessionId }.toSet()
        return programSessions.firstOrNull { it.id !in completedIds }
            ?: programSessions.firstOrNull()
    }
}
