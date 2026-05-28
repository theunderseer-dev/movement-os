package com.theunderseer.movementos.data.ai.cache

import com.theunderseer.movementos.data.ai.LlmResponse
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class InMemoryLlmResponseCache(
    private val maxSize: Int = DEFAULT_MAX_SIZE,
    private val clock: Clock = Clock.System,
) : LlmResponseCache {
    private val cache = mutableMapOf<String, CacheEntry>()
    private val accessOrder = mutableListOf<String>()
    private val mutex = Mutex()

    override suspend fun get(key: String): LlmResponse? =
        mutex.withLock {
            val entry = cache[key] ?: return@withLock null
            if (clock.now().toEpochMilliseconds() > entry.expiresAtMs) {
                cache.remove(key)
                accessOrder.remove(key)
                null
            } else {
                // Move to end (most recently used)
                accessOrder.remove(key)
                accessOrder.add(key)
                entry.response
            }
        }

    override suspend fun put(
        key: String,
        response: LlmResponse,
        ttlMs: Long,
    ) {
        mutex.withLock {
            cache[key] =
                CacheEntry(
                    response = response,
                    expiresAtMs = clock.now().toEpochMilliseconds() + ttlMs,
                )
            accessOrder.remove(key)
            accessOrder.add(key)

            while (cache.size > maxSize && accessOrder.isNotEmpty()) {
                val oldest = accessOrder.removeAt(0)
                cache.remove(oldest)
            }
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            cache.clear()
            accessOrder.clear()
        }
    }

    override suspend fun size(): Int = mutex.withLock { cache.size }

    private data class CacheEntry(
        val response: LlmResponse,
        val expiresAtMs: Long,
    )

    private companion object {
        const val DEFAULT_MAX_SIZE = 50
    }
}
