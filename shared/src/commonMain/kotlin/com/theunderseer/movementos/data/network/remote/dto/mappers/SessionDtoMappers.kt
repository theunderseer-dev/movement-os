package com.theunderseer.movementos.data.network.remote.dto.mappers

import com.theunderseer.movementos.data.network.remote.dto.SessionDto
import com.theunderseer.movementos.domain.model.Session
import com.theunderseer.movementos.domain.model.values.Duration

internal fun SessionDto.toDomain(): Session =
    Session(
        id = id,
        programId = programId,
        orderIndex = orderIndex,
        exercises = exercises.map { it.toDomain() },
        estimatedDuration = Duration(estimatedDurationSeconds),
    )

internal fun Session.toDto(): SessionDto =
    SessionDto(
        id = id,
        programId = programId,
        orderIndex = orderIndex,
        estimatedDurationSeconds = estimatedDuration.seconds,
        exercises = exercises.map { it.toDto(id, orderIndex) },
    )
