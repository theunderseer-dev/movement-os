package com.theunderseer.movementos.data.ai.adapter.anthropic.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class AnthropicContent(
    val type: String,
    val text: String,
)
