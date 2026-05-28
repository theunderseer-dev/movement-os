package com.theunderseer.movementos.data.ai.ratelimit

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Token bucket rate limiter.
 *
 * Bucket refills at rate of [config.maxRequests] tokens per [config.perDuration].
 * Each call consumes 1 token. When empty, `acquire()` returns false immediately;
 * `acquireOrWait()` suspends until token available.
 *
 * Less complex than sliding window (sufficient for client-side rate limiting).
 */
@OptIn(ExperimentalTime::class)
class TokenBucketRateLimiter(
    private val config: RateLimitConfig,
    private val clock: Clock = Clock.System,
) : RateLimiter {
    private var tokens: Double = config.maxRequests.toDouble()
    private var lastRefillMs: Long = clock.now().toEpochMilliseconds()
    private val mutex = Mutex()

    private val refillRatePerMs: Double =
        config.maxRequests.toDouble() / config.perDuration.inWholeMilliseconds

    override suspend fun acquire(): Boolean =
        mutex.withLock {
            refill()
            if (tokens >= 1.0) {
                tokens -= 1.0
                true
            } else {
                false
            }
        }

    override suspend fun acquireOrWait(): Boolean {
        while (true) {
            val acquired = acquire()
            if (acquired) return true
            val waitMs = calculateWaitTime()
            delay(waitMs)
        }
    }

    private fun refill() {
        val now = clock.now().toEpochMilliseconds()
        val elapsed = now - lastRefillMs
        val tokensToAdd = elapsed * refillRatePerMs
        tokens = (tokens + tokensToAdd).coerceAtMost(config.maxRequests.toDouble())
        lastRefillMs = now
    }

    private fun calculateWaitTime(): Long {
        val tokensNeeded = 1.0 - tokens
        return (tokensNeeded / refillRatePerMs).toLong().coerceAtLeast(MIN_WAIT_MS)
    }

    private companion object {
        const val MIN_WAIT_MS = 100L
    }
}
