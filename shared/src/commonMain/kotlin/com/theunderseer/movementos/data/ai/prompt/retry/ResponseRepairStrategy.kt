package com.theunderseer.movementos.data.ai.prompt.retry

import com.theunderseer.movementos.data.ai.LlmClient
import com.theunderseer.movementos.data.ai.LlmRequest

/**
 * On parse failure, asks the LLM to repair its own output.
 *
 * Useful for malformed JSON (missing quotes, trailing commas) — LLM can usually
 * fix these when shown the error. Skipped for semantic errors (wrong enum values).
 *
 * Single retry only — no recursion. If repair fails, caller falls back to
 * deterministic generation.
 */
internal class ResponseRepairStrategy(
    private val llmClient: LlmClient,
) {
    suspend fun repair(
        originalResponse: String,
        parseError: String,
    ): String? {
        val request =
            LlmRequest(
                systemPrompt = """
                You produced malformed JSON in your previous response.
                Return ONLY valid JSON matching the original schema.
                No explanations, no markdown.
            """,
                userPrompt = """
                Previous response (malformed):
                $originalResponse

                Error: $parseError

                Return the corrected JSON now.
            """,
                temperature = 0.1,
                maxTokens = LlmRequest.DEFAULT_MAX_TOKENS,
                responseFormat = LlmRequest.ResponseFormat.JSON,
            )

        return when (val result = llmClient.complete(request)) {
            is com.theunderseer.movementos.data.network.ApiResult.Success -> result.data.content
            is com.theunderseer.movementos.data.network.ApiResult.Error -> null
        }
    }
}
