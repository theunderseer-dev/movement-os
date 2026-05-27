package com.theunderseer.movementos.data.ai.ratelimit

import kotlin.time.Duration

/**
 * Configuration for rate limiter.
 *
 * Tuned per-provider to match their limits:
 * - Gemini Flash free: 15 req/min
 * - Anthropic free: ~5 req/min
 * - OpenAI free: 3 req/min (very strict)
 */
data class RateLimitConfig(
    val maxRequests: Int,
    val perDuration: Duration,
)
