package com.theunderseer.movementos.data.network.remote.dto.mappers

import ProgramDto
import com.theunderseer.movementos.domain.model.Program
import com.theunderseer.movementos.domain.model.values.MovementType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal fun ProgramDto.toDomain(): Program =
    Program(
        id = id,
        goalId = goalId,
        name = name,
        description = description,
        primaryType = MovementType.valueOf(primaryType),
        sessions = sessions.map { it.toDomain() },
        generatedAt = Instant.parse(generatedAt),
        isActive = isActive,
    )

@OptIn(ExperimentalTime::class)
internal fun Program.toDto(): ProgramDto =
    ProgramDto(
        id = id,
        goalId = goalId,
        name = name,
        description = description,
        primaryType = primaryType.name,
        generatedAt = generatedAt.toString(),
        isActive = isActive,
        sessions = sessions.map { it.toDto() },
    )
