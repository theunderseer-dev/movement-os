package com.theunderseer.movementos.data.repository

import com.theunderseer.movementos.data.local.ProgramLocalDataSource
import com.theunderseer.movementos.domain.model.Program
import com.theunderseer.movementos.domain.repository.ProgramRepository
import kotlinx.coroutines.flow.Flow

/**
 * Local-only implementation of [ProgramRepository].
 *
 * Reads come from SQLDelight as Flow — UI updates reactively on writes.
 * Remote sync is layered on top in Phase 3, not here.
 */
internal class DefaultProgramRepository(
    private val local: ProgramLocalDataSource,
) : ProgramRepository {
    override fun observeActiveProgram(): Flow<Program?> = local.observeActive()

    override suspend fun getById(id: String): Program? = local.getById(id)

    override suspend fun save(program: Program) = local.save(program)

    override suspend fun deactivate(id: String) = local.deactivate(id)
}
