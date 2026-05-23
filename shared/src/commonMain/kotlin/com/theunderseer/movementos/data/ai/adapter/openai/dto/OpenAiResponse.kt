package com.theunderseer.movementos.data.ai.adapter.openai.dto

import com.theunderseer.movementos.data.ai.adapter.openai.OpenAiChoice
import com.theunderseer.movementos.data.ai.adapter.openai.OpenAiUsage
import kotlinx.serialization.Serializable

@Serializable
internal data class OpenAiResponse(
    val id: String,
    val model: String,
    val choices: List<OpenAiChoice>,
    val usage: OpenAiUsage,
)
