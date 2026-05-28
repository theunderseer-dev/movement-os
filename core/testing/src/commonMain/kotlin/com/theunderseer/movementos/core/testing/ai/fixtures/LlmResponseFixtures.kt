package com.theunderseer.movementos.core.testing.ai.fixtures

import com.theunderseer.movementos.data.ai.LlmProvider
import com.theunderseer.movementos.data.ai.LlmResponse
import com.theunderseer.movementos.data.ai.TokenUsage

/**
 * Canned LLM responses for testing.
 *
 * Use Object Mother pattern — override only the fields the test cares about:
 * ```
 * aLlmResponse(content = "...", provider = OPENAI)
 * aLlmResponseWithTokenUsage(prompt = 500, completion = 1000)
 * ```
 */
object LlmResponseFixtures {
    fun aLlmResponse(
        content: String = "default content",
        provider: LlmProvider = LlmProvider.GEMINI,
        model: String = "gemini-2.0-flash",
        usage: TokenUsage = aTokenUsage(),
    ): LlmResponse =
        LlmResponse(
            content = content,
            provider = provider,
            model = model,
            usage = usage,
        )

    fun aTokenUsage(
        promptTokens: Int = 100,
        completionTokens: Int = 200,
        totalTokens: Int = 300,
    ): TokenUsage =
        TokenUsage(
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            totalTokens = totalTokens,
        )
}
