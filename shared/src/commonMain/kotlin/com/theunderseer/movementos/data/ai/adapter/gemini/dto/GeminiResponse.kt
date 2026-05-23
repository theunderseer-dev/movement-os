package com.theunderseer.movementos.data.ai.adapter.gemini.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GeminiResponse(
    val candidates: List<GeminiCandidate>,
    @SerialName("usageMetadata") val usageMetadata: GeminiUsage? = null,
)
