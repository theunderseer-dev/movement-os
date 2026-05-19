package com.theunderseer.movementos.domain.fake

import com.theunderseer.movementos.domain.common.DataError
import com.theunderseer.movementos.domain.common.DataState
import com.theunderseer.movementos.domain.model.ProgressEntry
import com.theunderseer.movementos.domain.model.Session
import com.theunderseer.movementos.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSessionRepository : SessionRepository {
    private val stateFlow = MutableStateFlow<DataState<List<ProgressEntry>>>(DataState.Success(emptyList()))

    fun setHistory(entries: List<ProgressEntry>) {
        stateFlow.value = DataState.Success(entries)
    }

    fun setLoading(cached: List<ProgressEntry>? = null) {
        stateFlow.value = DataState.Loading(cached)
    }

    fun setError(cached: List<ProgressEntry>? = null) {
        stateFlow.value = DataState.Error(DataError.Network, cached)
    }

    override suspend fun recordCompletion(entry: ProgressEntry) {
        stateFlow.value = DataState.Success(currentEntries() + entry)
    }

    override fun observeProgressHistory(forceRefresh: Boolean): Flow<DataState<List<ProgressEntry>>> = stateFlow

    override suspend fun getProgressForSession(sessionId: String): List<ProgressEntry> =
        currentEntries().filter { it.sessionId == sessionId }

    override suspend fun getNextSession(programSessions: List<Session>): Session? {
        val completedIds = currentEntries().map { it.sessionId }.toSet()
        return programSessions.firstOrNull { it.id !in completedIds }
            ?: programSessions.firstOrNull()
    }

    private fun currentEntries(): List<ProgressEntry> =
        when (val s = stateFlow.value) {
            is DataState.Success -> s.data
            is DataState.Loading -> s.cached ?: emptyList()
            is DataState.Error -> s.cached ?: emptyList()
        }
}
