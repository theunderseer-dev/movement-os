package com.theunderseer.movementos.data.ai.adapter.gemini

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GeminiRequest(
    val contents: List<GeminiContent>,
    @SerialName("generationConfig") val generationConfig: GeminiGenerationConfig,
    @SerialName("systemInstruction") val systemInstruction: GeminiContent? = null,
)

@Serializable
internal data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>,
)

@Serializable
internal data class GeminiPart(
    val text: String,
)

@Serializable
internal data class GeminiGenerationConfig(
    val temperature: Double,
    @SerialName("maxOutputTokens") val maxOutputTokens: Int,
    @SerialName("responseMimeType") val responseMimeType: String? = null,
)
