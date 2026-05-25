package com.theunderseer.movementos.data.ai.prompt.program

import com.theunderseer.movementos.data.ai.prompt.ResponseValidationError
import com.theunderseer.movementos.domain.model.Exercise
import com.theunderseer.movementos.domain.model.Program
import com.theunderseer.movementos.domain.model.Session
import com.theunderseer.movementos.domain.model.UserGoal
import com.theunderseer.movementos.domain.model.values.Duration
import com.theunderseer.movementos.domain.model.values.MovementType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Maps LLM-parsed response to domain [Program].
 *
 * Validation here is **semantic** (e.g., MovementType.valueOf throws on unknown enum) —
 * structural validation already done by kotlinx.serialization. Returns Result so
 * caller can fall back to deterministic generation on semantic violations.
 *
 * Generates IDs deterministically from goal + indices so retries don't accumulate
 * duplicate Programs in the DB.
 */
@OptIn(ExperimentalTime::class)
internal fun ProgramGenerationResponse.toDomain(
    goal: UserGoal,
    generatedAt: Instant,
): Result<Program> =
    runCatching {
        val programId = "program-${goal.id}-${generatedAt.toEpochMilliseconds()}"

        val mappedSessions =
            sessions.map { sessionResponse ->
                val sessionId = "$programId-session-${sessionResponse.orderIndex}"
                Session(
                    id = sessionId,
                    programId = programId,
                    orderIndex = sessionResponse.orderIndex,
                    exercises =
                        sessionResponse.exercises.mapIndexed { index, exerciseResponse ->
                            Exercise(
                                id = "$sessionId-exercise-$index",
                                name = exerciseResponse.name,
                                duration = Duration(exerciseResponse.durationSeconds),
                                type = MovementType.valueOf(exerciseResponse.type),
                                cues = exerciseResponse.cues,
                            )
                        },
                    estimatedDuration = Duration.ofMinutes(sessionResponse.estimatedDurationMinutes),
                )
            }

        Program(
            id = programId,
            goalId = goal.id,
            name = name,
            description = description,
            primaryType = MovementType.valueOf(primaryType),
            sessions = mappedSessions,
            generatedAt = generatedAt,
            isActive = true,
        )
    }

/**
 * Maps mapping failures to typed validation errors.
 */
internal fun Throwable.toValidationError(): ResponseValidationError =
    when (this) {
        is IllegalArgumentException -> {
            ResponseValidationError.UnexpectedValue(
                field = "primary_type or exercise.type",
                value = message ?: "unknown",
                expected = "one of MovementType values",
            )
        }

        else -> {
            ResponseValidationError.Unknown(message ?: "Unknown mapping error")
        }
    }
