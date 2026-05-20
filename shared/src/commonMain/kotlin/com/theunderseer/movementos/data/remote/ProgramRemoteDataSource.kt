package com.theunderseer.movementos.data.remote

import com.theunderseer.movementos.domain.model.Program

/**
 * Remote data source for Programs.
 */
internal interface ProgramRemoteDataSource {
    suspend fun getActive(): Program?

    suspend fun upsert(program: Program)
}
