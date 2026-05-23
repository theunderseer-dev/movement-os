package com.theunderseer.movementos.data.ai.adapter.gemini.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GeminiGenerationConfig(
    val temperature: Double,
    @SerialName("maxOutputTokens") val maxOutputTokens: Int,
    @SerialName("responseMimeType") val responseMimeType: String? = null,
)
