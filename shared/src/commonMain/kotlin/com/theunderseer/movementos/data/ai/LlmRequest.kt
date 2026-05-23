package com.theunderseer.movementos.data.ai

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Provider-agnostic request to an LLM.
 *
 * Adapters translate this to provider-specific schemas (OpenAI chat completions,
 * Claude messages, Gemini generateContent).
 */
data class LlmRequest(
    val systemPrompt: String?,
    val userPrompt: String,
    val maxTokens: Int = DEFAULT_MAX_TOKENS,
    val temperature: Double = DEFAULT_TEMPERATURE,
    val timeout: Duration = DEFAULT_TIMEOUT,
    val responseFormat: ResponseFormat = ResponseFormat.TEXT,
) {
    enum class ResponseFormat { TEXT, JSON }

    companion object {
        const val DEFAULT_MAX_TOKENS = 2048
        const val DEFAULT_TEMPERATURE = 0.7
        val DEFAULT_TIMEOUT: Duration = 30.seconds
    }
}
