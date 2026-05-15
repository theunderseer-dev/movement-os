package com.theunderseer.movementos.data.local

import com.theunderseer.movementos.database.MovementOSDatabase
import com.theunderseer.movementos.domain.model.Exercise
import com.theunderseer.movementos.domain.model.Session
import com.theunderseer.movementos.domain.model.values.Duration
import com.theunderseer.movementos.domain.model.values.MovementType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class SessionLocalDataSource(
    private val database: MovementOSDatabase,
    private val dispatcher: CoroutineDispatcher,
) {
    private val sessionsQueries get() = database.sessionsQueries
    private val exercisesQueries get() = database.exercisesQueries

    suspend fun getByProgramId(programId: String): List<Session> =
        withContext(dispatcher) {
            sessionsQueries.selectByProgramId(programId).executeAsList().map { row ->
                val exercises =
                    exercisesQueries.selectBySessionId(row.id).executeAsList().map { ex ->
                        Exercise(
                            id = ex.id,
                            name = ex.name,
                            duration = Duration(ex.duration_seconds.toInt()),
                            type = MovementType.valueOf(ex.type),
                            cues = Json.decodeFromString(ex.cues),
                        )
                    }
                Session(
                    id = row.id,
                    programId = row.program_id,
                    orderIndex = row.order_index.toInt(),
                    exercises = exercises,
                    estimatedDuration = Duration(row.estimated_duration_seconds.toInt()),
                )
            }
        }

    suspend fun replaceForProgram(
        programId: String,
        sessions: List<Session>,
    ) = withContext(dispatcher) {
        database.transaction {
            val existingIds = sessionsQueries.selectByProgramId(programId).executeAsList().map { it.id }
            existingIds.forEach { exercisesQueries.deleteBySessionId(it) }
            sessionsQueries.deleteByProgramId(programId)
            sessions.forEach { session ->
                sessionsQueries.upsert(
                    id = session.id,
                    program_id = session.programId,
                    order_index = session.orderIndex.toLong(),
                    estimated_duration_seconds = session.estimatedDuration.seconds.toLong(),
                )
                session.exercises.forEachIndexed { index, exercise ->
                    exercisesQueries.upsert(
                        id = exercise.id,
                        session_id = session.id,
                        order_index = index.toLong(),
                        name = exercise.name,
                        duration_seconds = exercise.duration.seconds.toLong(),
                        type = exercise.type.name,
                        cues = Json.encodeToString(exercise.cues),
                    )
                }
            }
        }
    }
}
