package com.theunderseer.movementos.data.ai.adapter.gemini

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GeminiResponse(
    val candidates: List<GeminiCandidate>,
    @SerialName("usageMetadata") val usageMetadata: GeminiUsage? = null,
)

@Serializable
internal data class GeminiCandidate(
    val content: GeminiContent,
    @SerialName("finishReason") val finishReason: String? = null,
)

@Serializable
internal data class GeminiUsage(
    @SerialName("promptTokenCount") val promptTokenCount: Int = 0,
    @SerialName("candidatesTokenCount") val candidatesTokenCount: Int = 0,
    @SerialName("totalTokenCount") val totalTokenCount: Int = 0,
)
