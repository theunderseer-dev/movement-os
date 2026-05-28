package com.theunderseer.movementos.core.testing.ai

import com.theunderseer.movementos.data.ai.LlmResponse
import com.theunderseer.movementos.data.ai.cache.LlmResponseCache

/**
 * In-memory cache fake for tests.
 *
 * Tracks operations:
 * ```
 * assertTrue(cache.containsKey("key-1"))
 * assertEquals(2, cache.putCount)
 * assertEquals(1, cache.hitCount)
 * ```
 */
class FakeLlmResponseCache : LlmResponseCache {
    private val entries = mutableMapOf<String, LlmResponse>()
    var putCount: Int = 0
        private set
    var hitCount: Int = 0
        private set
    var missCount: Int = 0
        private set

    override suspend fun get(key: String): LlmResponse? {
        val response = entries[key]
        if (response != null) hitCount++ else missCount++
        return response
    }

    override suspend fun put(
        key: String,
        response: LlmResponse,
        ttlMs: Long,
    ) {
        entries[key] = response
        putCount++
    }

    override suspend fun clear() {
        entries.clear()
    }

    override suspend fun size(): Int = entries.size

    fun containsKey(key: String): Boolean = entries.containsKey(key)

    fun keys(): Set<String> = entries.keys.toSet()
}
