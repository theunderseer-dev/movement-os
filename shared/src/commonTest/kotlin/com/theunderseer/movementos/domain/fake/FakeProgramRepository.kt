package com.theunderseer.movementos.domain.fake

import com.theunderseer.movementos.domain.model.Program
import com.theunderseer.movementos.domain.repository.ProgramRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class FakeProgramRepository : ProgramRepository {
    private val programs = mutableMapOf<String, Program>()
    private val activeProgramFlow = MutableStateFlow<Program?>(null)

    override fun observeActiveProgram(): Flow<Program?> = activeProgramFlow

    override suspend fun getById(id: String): Program? = programs[id]

    override suspend fun save(program: Program) {
        programs.values.forEach { existing ->
            if (existing.isActive) programs[existing.id] = existing.copy(isActive = false)
        }
        val active = program.copy(isActive = true)
        programs[program.id] = active
        activeProgramFlow.value = active
    }

    override suspend fun deactivate(id: String) {
        programs[id]?.let { programs[id] = it.copy(isActive = false) }
        if (activeProgramFlow.value?.id == id) activeProgramFlow.value = null
    }
}
