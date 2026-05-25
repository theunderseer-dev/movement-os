package com.theunderseer.movementos.data.ai.prompt

import com.theunderseer.movementos.data.ai.LlmRequest

/**
 * Builds an [LlmRequest] from a [PromptTemplate] and [PromptContext].
 *
 * Centralizes request construction so callers don't manually set max_tokens,
 * temperature, response_format for each prompt type.
 */
internal class PromptBuilder {
    fun build(
        template: PromptTemplate,
        context: PromptContext,
        config: PromptConfig = PromptConfig.STRUCTURED_JSON,
    ): LlmRequest {
        val rendered = template.render(context)
        return LlmRequest(
            systemPrompt = rendered.systemPrompt,
            userPrompt = rendered.userPrompt,
            maxTokens = config.maxTokens,
            temperature = config.temperature,
            responseFormat = config.responseFormat,
        )
    }
}

/**
 * Predefined configs for common prompt types.
 *
 * Structured JSON: low temperature for deterministic output, JSON response format,
 * larger token budget for nested structures.
 */
data class PromptConfig(
    val temperature: Double,
    val maxTokens: Int,
    val responseFormat: LlmRequest.ResponseFormat,
) {
    companion object {
        val STRUCTURED_JSON =
            PromptConfig(
                temperature = 0.3,
                maxTokens = 4096,
                responseFormat = LlmRequest.ResponseFormat.JSON,
            )
        val CONVERSATIONAL =
            PromptConfig(
                temperature = 0.7,
                maxTokens = 1024,
                responseFormat = LlmRequest.ResponseFormat.TEXT,
            )
    }
}
