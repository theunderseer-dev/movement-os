package com.theunderseer.movementos.data.ai.cache

import com.theunderseer.movementos.data.ai.LlmResponse

/**
 * Cache for LLM responses keyed by request fingerprint.
 *
 * Implementations decide TTL, eviction, storage backend.
 */
interface LlmResponseCache {
    suspend fun get(key: String): LlmResponse?

    suspend fun put(
        key: String,
        response: LlmResponse,
        ttlMs: Long = DEFAULT_TTL_MS,
    )

    suspend fun clear()

    suspend fun size(): Int

    companion object {
        const val DEFAULT_TTL_MS: Long = 24 * 60 * 60 * 1000 // 24 hours
    }
}
