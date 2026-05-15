package com.theunderseer.movementos.data.local

import app.cash.sqldelight.coroutines.asFlow
import com.theunderseer.movementos.data.local.mappers.toDomain
import com.theunderseer.movementos.database.MovementOSDatabase
import com.theunderseer.movementos.domain.model.Program
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Local data source for Programs. Wraps SQLDelight queries in domain types.
 *
 * Uses IO dispatcher for all suspend operations. Flow operations transform
 * results on the same dispatcher to avoid context-switching overhead.
 */
@OptIn(ExperimentalTime::class)
internal class ProgramLocalDataSource(
    private val database: MovementOSDatabase,
    private val sessionDataSource: SessionLocalDataSource,
    private val dispatcher: CoroutineDispatcher,
    private val clock: Clock = Clock.System,
) {
    private val queries get() = database.programsQueries

    fun observeActive(): Flow<Program?> =
        queries.observeActive().asFlow().map { query ->
            val row = withContext(dispatcher) { query.executeAsOneOrNull() } ?: return@map null
            val sessions = sessionDataSource.getByProgramId(row.id)
            row.toDomain(sessions)
        }

    suspend fun getById(id: String): Program? =
        withContext(dispatcher) {
            val row = queries.selectById(id).executeAsOneOrNull() ?: return@withContext null
            row.toDomain(sessionDataSource.getByProgramId(id))
        }

    suspend fun save(program: Program) =
        withContext(dispatcher) {
            database.transaction {
                queries.deactivateAll(now = clock.now().toEpochMilliseconds())
                queries.upsert(
                    id = program.id,
                    goal_id = program.goalId,
                    name = program.name,
                    description = program.description,
                    primary_type = program.primaryType.name,
                    generated_at_ms = program.generatedAt.toEpochMilliseconds(),
                    is_active = if (program.isActive) 1L else 0L,
                    updated_at_ms = clock.now().toEpochMilliseconds(),
                    is_dirty = 1L,
                )
            }
            sessionDataSource.replaceForProgram(program.id, program.sessions)
        }

    suspend fun deactivate(id: String) =
        withContext(dispatcher) {
            queries.deactivateById(id = id, now = clock.now().toEpochMilliseconds())
        }
}
