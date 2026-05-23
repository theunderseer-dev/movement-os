package com.theunderseer.movementos.data.ai.adapter.gemini.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class GeminiPart(
    val text: String,
)
