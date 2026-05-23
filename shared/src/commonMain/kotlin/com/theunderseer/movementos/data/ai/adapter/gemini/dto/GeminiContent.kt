package com.theunderseer.movementos.data.ai.adapter.gemini.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>,
)
