package com.theunderseer.movementos.data.ai.ratelimit

/**
 * Controls request frequency to prevent provider rate-limit errors.
 */
interface RateLimiter {
    suspend fun acquire(): Boolean

    suspend fun acquireOrWait(): Boolean
}
