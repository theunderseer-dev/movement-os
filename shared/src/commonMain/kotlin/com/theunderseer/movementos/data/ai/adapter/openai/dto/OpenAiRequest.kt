package com.theunderseer.movementos.data.ai.adapter.openai.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OpenAiRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    @SerialName("max_tokens") val maxTokens: Int,
    val temperature: Double,
    @SerialName("response_format") val responseFormat: OpenAiResponseFormat? = null,
)
