package com.theunderseer.movementos.data.ai

/**
 * Provider-agnostic response from an LLM.
 */
data class LlmResponse(
    val content: String,
    val provider: LlmProvider,
    val model: String,
    val usage: TokenUsage,
)

data class TokenUsage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
)
