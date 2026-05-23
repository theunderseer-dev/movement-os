package com.theunderseer.movementos.data.ai.adapter.anthropic.model

import kotlinx.serialization.Serializable

@Serializable
internal data class AnthropicMessage(
    val role: String,
    val content: String,
)
