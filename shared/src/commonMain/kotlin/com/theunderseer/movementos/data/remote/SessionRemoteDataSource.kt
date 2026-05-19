package com.theunderseer.movementos.data.remote

import com.theunderseer.movementos.domain.model.ProgressEntry

internal interface SessionRemoteDataSource {
    suspend fun recordCompletion(entry: ProgressEntry)

    suspend fun getProgressHistory(): List<ProgressEntry>

    suspend fun getProgressForSession(sessionId: String): List<ProgressEntry>
}

internal class StubSessionRemoteDataSource : SessionRemoteDataSource {
    override suspend fun recordCompletion(entry: ProgressEntry) = Unit

    override suspend fun getProgressHistory(): List<ProgressEntry> = emptyList()

    override suspend fun getProgressForSession(sessionId: String): List<ProgressEntry> = emptyList()
}
