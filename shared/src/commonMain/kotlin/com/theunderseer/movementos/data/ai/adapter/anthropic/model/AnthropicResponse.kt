package com.theunderseer.movementos.data.ai.adapter.anthropic.model

import kotlinx.serialization.Serializable

@Serializable
internal data class AnthropicResponse(
    val id: String,
    val content: List<AnthropicContent>,
    val model: String,
    val usage: AnthropicUsage,
)
