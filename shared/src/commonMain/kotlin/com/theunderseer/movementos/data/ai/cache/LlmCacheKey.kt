package com.theunderseer.movementos.data.ai.cache

import com.theunderseer.movementos.data.ai.LlmRequest

/**
 * Deterministic cache key for an LLM request.
 *
 * Same prompt + same config = same key, regardless of provider chosen.
 * Cache hits transparent to caller. They get cached response without knowing.
 *
 * Excludes timeout from hash (irrelevant to response content).
 */
internal object LlmCacheKey {
    fun forRequest(request: LlmRequest): String =
        buildString {
            append(hashCode(request.systemPrompt ?: ""))
            append("-")
            append(hashCode(request.userPrompt))
            append("-")
            append(request.maxTokens)
            append("-")
            append((request.temperature * MULTIPLIER).toInt())
            append("-")
            append(request.responseFormat.name)
        }

    private fun hashCode(s: String): String {
        var hash = 0L
        for (c in s) {
            hash = hash * PRIME + c.code
        }
        return hash.toString(BASE)
    }

    private const val MULTIPLIER = 100
    private const val PRIME = 31L
    private const val BASE = 36 // base-36 for shorter keys
}
