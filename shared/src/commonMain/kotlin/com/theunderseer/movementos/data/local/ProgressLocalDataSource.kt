package com.theunderseer.movementos.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.theunderseer.movementos.database.MovementOSDatabase
import com.theunderseer.movementos.database.Progress
import com.theunderseer.movementos.domain.model.ProgressEntry
import com.theunderseer.movementos.domain.model.values.DifficultyLevel
import com.theunderseer.movementos.domain.model.values.Duration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal class ProgressLocalDataSource(
    private val database: MovementOSDatabase,
    private val dispatcher: CoroutineDispatcher,
    private val clock: Clock = Clock.System,
) {
    private val queries get() = database.progressQueries

    fun observeAll(): Flow<List<ProgressEntry>> =
        queries
            .observeAll()
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.map { it.toDomain() } }

    suspend fun getBySessionId(sessionId: String): List<ProgressEntry> =
        withContext(dispatcher) {
            queries.selectBySessionId(sessionId).executeAsList().map { it.toDomain() }
        }

    suspend fun upsert(entry: ProgressEntry) =
        withContext(dispatcher) {
            queries.upsert(
                id = entry.id,
                session_id = entry.sessionId,
                completed_at_ms = entry.completedAt.toEpochMilliseconds(),
                actual_duration_seconds = entry.actualDuration.seconds.toLong(),
                perceived_difficulty = entry.perceivedDifficulty.name,
                notes = entry.notes,
                updated_at_ms = clock.now().toEpochMilliseconds(),
                is_dirty = 1L,
            )
        }

    private fun Progress.toDomain(): ProgressEntry =
        ProgressEntry(
            id = id,
            sessionId = session_id,
            completedAt = Instant.fromEpochMilliseconds(completed_at_ms),
            actualDuration = Duration(actual_duration_seconds.toInt()),
            perceivedDifficulty = DifficultyLevel.valueOf(perceived_difficulty),
            notes = notes,
        )
}
