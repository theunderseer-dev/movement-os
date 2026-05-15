package com.theunderseer.movementos.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.theunderseer.movementos.database.Goals
import com.theunderseer.movementos.database.MovementOSDatabase
import com.theunderseer.movementos.domain.model.UserGoal
import com.theunderseer.movementos.domain.model.values.Duration
import com.theunderseer.movementos.domain.model.values.MovementType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal class GoalLocalDataSource(
    private val database: MovementOSDatabase,
    private val dispatcher: CoroutineDispatcher,
    private val clock: Clock = Clock.System,
) {
    private val queries get() = database.goalsQueries

    fun observeCurrent(): Flow<UserGoal?> =
        queries
            .observeCurrent()
            .asFlow()
            .mapToOneOrNull(dispatcher)
            .map { it?.toDomain() }

    suspend fun getById(id: String): UserGoal? =
        withContext(dispatcher) {
            queries.selectById(id).executeAsOneOrNull()?.toDomain()
        }

    suspend fun save(goal: UserGoal) =
        withContext(dispatcher) {
            queries.upsert(
                id = goal.id,
                description = goal.description,
                focus = goal.focus.name,
                sessions_per_week = goal.sessionsPerWeek.toLong(),
                time_per_session_seconds = goal.timePerSession.seconds.toLong(),
                created_at_ms = goal.createdAt.toEpochMilliseconds(),
                updated_at_ms = clock.now().toEpochMilliseconds(),
                is_dirty = 1L,
            )
        }

    private fun Goals.toDomain(): UserGoal =
        UserGoal(
            id = id,
            description = description,
            focus = MovementType.valueOf(focus),
            sessionsPerWeek = sessions_per_week.toInt(),
            timePerSession = Duration(time_per_session_seconds.toInt()),
            createdAt = Instant.fromEpochMilliseconds(created_at_ms),
        )
}
