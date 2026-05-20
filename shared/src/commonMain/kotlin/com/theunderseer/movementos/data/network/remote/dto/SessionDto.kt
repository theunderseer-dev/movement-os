package com.theunderseer.movementos.data.network.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SessionDto(
    val id: String,
    @SerialName("program_id") val programId: String,
    @SerialName("order_index") val orderIndex: Int,
    @SerialName("estimated_duration_seconds") val estimatedDurationSeconds: Int,
    val exercises: List<ExerciseDto> = emptyList(),
)
