package com.theunderseer.movementos.data.network.llm.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LlmCompletionRequest(
    val prompt: String,
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int = 1024,
    val temperature: Double = 0.7,
)

@Serializable
data class LlmCompletionResponse(
    val id: String,
    val content: String,
    @SerialName("model") val model: String,
    val usage: LlmUsage? = null,
)

@Serializable
data class LlmUsage(
    @SerialName("prompt_tokens") val promptTokens: Int,
    @SerialName("completion_tokens") val completionTokens: Int,
    @SerialName("total_tokens") val totalTokens: Int,
)
