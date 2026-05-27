package com.theunderseer.movementos.data.ai.ratelimit

import co.touchlab.kermit.Logger
import com.theunderseer.movementos.data.ai.LlmClient
import com.theunderseer.movementos.data.ai.LlmRequest
import com.theunderseer.movementos.data.ai.LlmResponse
import com.theunderseer.movementos.data.network.ApiResult

/**
 * Decorator applying rate limiting before delegating to underlying client.
 *
 * Behavior: waits (not fails) when rate limit exceeded. Prefers latency over errors.
 * For user-driven actions this is correct (user can wait); background work can
 * configure shorter timeout if needed.
 */
internal class RateLimitedLlmClient(
    private val delegate: LlmClient,
    private val limiter: RateLimiter,
    private val logger: Logger = Logger.withTag("RateLimitedLlm"),
) : LlmClient {
    override suspend fun complete(request: LlmRequest): ApiResult<LlmResponse> {
        if (!limiter.acquire()) {
            logger.d { "Rate limit hit, waiting for token..." }
            limiter.acquireOrWait()
        }
        return delegate.complete(request)
    }
}
