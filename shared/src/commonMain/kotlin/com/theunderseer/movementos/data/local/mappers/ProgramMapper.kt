package com.theunderseer.movementos.data.local.mappers

import com.theunderseer.movementos.database.Programs
import com.theunderseer.movementos.domain.model.Program
import com.theunderseer.movementos.domain.model.Session
import com.theunderseer.movementos.domain.model.values.MovementType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Converts SQLDelight row to domain Program.
 *
 * Sessions are loaded separately and passed in to keep mapper free of I/O.
 */
@OptIn(ExperimentalTime::class)
internal fun Programs.toDomain(sessions: List<Session>): Program =
    Program(
        id = id,
        goalId = goal_id,
        name = name,
        description = description,
        primaryType = MovementType.valueOf(primary_type),
        sessions = sessions,
        generatedAt = Instant.fromEpochMilliseconds(generated_at_ms),
        isActive = is_active == 1L,
    )
