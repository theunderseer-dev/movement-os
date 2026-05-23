package com.theunderseer.movementos.data.ai.adapter.openai.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class OpenAiResponse(
    val id: String,
    val model: String,
    val choices: List<OpenAiChoice>,
    val usage: OpenAiUsage,
)
