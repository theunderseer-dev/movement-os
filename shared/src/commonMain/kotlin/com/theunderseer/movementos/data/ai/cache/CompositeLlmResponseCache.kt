package com.theunderseer.movementos.data.ai.cache

import com.theunderseer.movementos.data.ai.LlmResponse

/**
 * L1 (memory) → L2 (disk) layered cache.
 *
 * Read: check L1 first, fall back to L2 (promotes hit back to L1).
 * Write: update both layers simultaneously.
 *
 * Reduces latency for hot keys (L1) while preserving cold keys across restarts (L2).
 */
internal class CompositeLlmResponseCache(
    private val l1: LlmResponseCache,
    private val l2: LlmResponseCache,
) : LlmResponseCache {
    override suspend fun get(key: String): LlmResponse? {
        l1.get(key)?.let { return it }
        return l2.get(key)?.also { promoted -> l1.put(key, promoted) }
    }

    override suspend fun put(
        key: String,
        response: LlmResponse,
        ttlMs: Long,
    ) {
        l1.put(key, response, ttlMs)
        l2.put(key, response, ttlMs)
    }

    override suspend fun clear() {
        l1.clear()
        l2.clear()
    }

    override suspend fun size(): Int = l2.size() // L2 is authoritative
}
