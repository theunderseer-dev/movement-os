package com.theunderseer.movementos.data.remote

import com.theunderseer.movementos.domain.model.ProgressEntry

internal interface SessionRemoteDataSource {
    suspend fun recordCompletion(entry: ProgressEntry)

    suspend fun getProgressHistory(): List<ProgressEntry>
}
