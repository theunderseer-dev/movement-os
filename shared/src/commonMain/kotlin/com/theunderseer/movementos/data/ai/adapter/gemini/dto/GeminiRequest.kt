package com.theunderseer.movementos.data.ai.adapter.gemini.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GeminiRequest(
    val contents: List<GeminiContent>,
    @SerialName("generationConfig") val generationConfig: GeminiGenerationConfig,
    @SerialName("systemInstruction") val systemInstruction: GeminiContent? = null,
)
