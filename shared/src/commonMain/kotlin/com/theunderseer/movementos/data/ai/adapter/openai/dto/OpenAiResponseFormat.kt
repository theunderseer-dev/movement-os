package com.theunderseer.movementos.data.ai.adapter.openai.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class OpenAiResponseFormat(
    val type: String,
)
