package com.theunderseer.movementos.data.ai.cache

import com.theunderseer.movementos.data.ai.LlmClient
import com.theunderseer.movementos.data.ai.LlmRequest
import com.theunderseer.movementos.data.ai.LlmResponse
import com.theunderseer.movementos.data.ai.telemetry.LlmEvent
import com.theunderseer.movementos.data.ai.telemetry.LlmTelemetry
import com.theunderseer.movementos.data.network.ApiResult
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Decorator caching LLM responses by request fingerprint.
 *
 * On cache hit: returns cached response immediately, emits CacheHit telemetry.
 * On cache miss: delegates, stores result if successful.
 *
 * Failed responses are NOT cached. Retry is correct behavior on next call.
 */
@OptIn(ExperimentalTime::class)
internal class CachedLlmClient(
    private val delegate: LlmClient,
    private val cache: LlmResponseCache,
    private val telemetry: LlmTelemetry,
    private val clock: Clock = Clock.System,
) : LlmClient {
    override suspend fun complete(request: LlmRequest): ApiResult<LlmResponse> {
        val key = LlmCacheKey.forRequest(request)

        cache.get(key)?.let { cached ->
            telemetry.emit(
                LlmEvent.CacheHit(
                    timestamp = clock.now(),
                    requestId = "cached-${clock.now().toEpochMilliseconds()}",
                    cacheKey = key,
                ),
            )
            return ApiResult.Success(cached)
        }

        val result = delegate.complete(request)
        if (result is ApiResult.Success) {
            cache.put(key, result.data)
        }
        return result
    }
}
