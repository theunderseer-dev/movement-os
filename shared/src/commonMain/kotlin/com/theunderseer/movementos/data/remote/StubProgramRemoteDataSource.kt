package com.theunderseer.movementos.data.remote

import com.theunderseer.movementos.domain.model.Program

internal class StubProgramRemoteDataSource : ProgramRemoteDataSource {
    override suspend fun getActive(): Program? = null

    override suspend fun upsert(program: Program) = Unit
}
