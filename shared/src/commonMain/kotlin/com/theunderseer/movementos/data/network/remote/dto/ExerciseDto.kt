package com.theunderseer.movementos.data.network.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ExerciseDto(
    val id: String,
    @SerialName("session_id") val sessionId: String,
    @SerialName("order_index") val orderIndex: Int,
    val name: String,
    @SerialName("duration_seconds") val durationSeconds: Int,
    val type: String,
    val cues: List<String> = emptyList(),
)
