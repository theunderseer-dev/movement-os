package com.theunderseer.movementos.data.remote

import com.theunderseer.movementos.domain.model.ProgressEntry

internal class StubSessionRemoteDataSource : SessionRemoteDataSource {
    override suspend fun recordCompletion(entry: ProgressEntry) = Unit

    override suspend fun getProgressHistory(): List<ProgressEntry> = emptyList()
}
