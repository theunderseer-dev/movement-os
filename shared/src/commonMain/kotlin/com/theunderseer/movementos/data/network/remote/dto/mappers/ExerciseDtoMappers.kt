package com.theunderseer.movementos.data.network.remote.dto.mappers

import com.theunderseer.movementos.data.network.remote.dto.ExerciseDto
import com.theunderseer.movementos.domain.model.Exercise
import com.theunderseer.movementos.domain.model.values.Duration
import com.theunderseer.movementos.domain.model.values.MovementType

internal fun ExerciseDto.toDomain(): Exercise =
    Exercise(
        id = id,
        name = name,
        duration = Duration(durationSeconds),
        type = MovementType.valueOf(type),
        cues = cues,
    )

internal fun Exercise.toDto(
    sessionId: String,
    orderIndex: Int,
): ExerciseDto =
    ExerciseDto(
        id = id,
        sessionId = sessionId,
        orderIndex = orderIndex,
        name = name,
        durationSeconds = duration.seconds,
        type = type.name,
        cues = cues,
    )
