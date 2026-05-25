package com.theunderseer.movementos.data.ai.prompt.program

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Top-level response from the LLM for a program generation request.
 *
 * Marked @Serializable for kotlinx.serialization parsing. All fields required —
 * missing fields fail parse and trigger ResponseRepairStrategy.
 */
@Serializable
internal data class ProgramGenerationResponse(
    val name: String,
    val description: String,
    @SerialName("primary_type") val primaryType: String,
    val sessions: List<ProgramSessionResponse>,
)

@Serializable
internal data class ProgramSessionResponse(
    @SerialName("order_index") val orderIndex: Int,
    @SerialName("estimated_duration_minutes") val estimatedDurationMinutes: Int,
    val exercises: List<ProgramExerciseResponse>,
)

@Serializable
internal data class ProgramExerciseResponse(
    val name: String,
    @SerialName("duration_seconds") val durationSeconds: Int,
    val type: String,
    val cues: List<String> = emptyList(),
)
