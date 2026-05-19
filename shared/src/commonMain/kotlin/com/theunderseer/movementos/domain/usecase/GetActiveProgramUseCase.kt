package com.theunderseer.movementos.domain.usecase

import com.theunderseer.movementos.domain.common.DataState
import com.theunderseer.movementos.domain.model.Program
import com.theunderseer.movementos.domain.repository.ProgramRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observes the user's currently active program.
 *
 * Returns Flow<DataState> so UI receives Loading/Success/Error transparently.
 * Forwarded directly from repository — use case adds no orchestration here.
 */
class GetActiveProgramUseCase(
    private val programRepository: ProgramRepository,
) {
    operator fun invoke(): Flow<DataState<Program>> = programRepository.observeActiveProgram()
}
