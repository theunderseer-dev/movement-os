package com.theunderseer.movementos.data.ai.orchestrator

import co.touchlab.kermit.Logger
import com.theunderseer.movementos.data.ai.LlmClient
import com.theunderseer.movementos.data.ai.LlmProvider
import com.theunderseer.movementos.data.ai.LlmRequest
import com.theunderseer.movementos.data.ai.LlmResponse
import com.theunderseer.movementos.data.ai.adapter.LlmAdapter
import com.theunderseer.movementos.data.network.ApiResult

/**
 * Routes LLM requests through providers with retry and fallback.
 *
 * Strategy (configurable via [LlmRequestStrategy]):
 * 1. Try primary provider
 * 2. On retryable error (rate limit, network, 5xx), try next provider
 * 3. On 4xx (excluding rate limit), fail fast, request itself is invalid
 * 4. Track provider health to skip recently-failing providers
 *
 * UI code depends on [LlmClient] interface, never on this orchestrator directly.
 */
internal class LlmOrchestrator(
    private val adapters: Map<LlmProvider, LlmAdapter>,
    private val strategy: LlmRequestStrategy,
    private val healthTracker: ProviderHealthTracker,
    private val logger: Logger = Logger.withTag("LlmOrchestrator"),
) : LlmClient {

    override suspend fun complete(request: LlmRequest): ApiResult<LlmResponse> {
        val providersInOrder = strategy.selectProviders(
            request = request,
            availableProviders = adapters.keys,
            healthTracker = healthTracker,
        )

        if (providersInOrder.isEmpty()) {
            logger.w { "No providers available for request" }
            return ApiResult.Error.Unknown("No providers available")
        }

        var lastError: ApiResult.Error? = null

        for (provider in providersInOrder) {
            val adapter = adapters[provider] ?: continue
            logger.d { "Attempting LLM call via $provider" }

            when (val result = adapter.complete(request)) {
                is ApiResult.Success -> {
                    healthTracker.recordSuccess(provider)
                    logger.d { "LLM call succeeded via $provider" }
                    return result
                }
                is ApiResult.Error -> {
                    lastError = result
                    healthTracker.recordFailure(provider, result)
                    logger.w { "LLM call failed via $provider: $result" }

                    if (!isRetryable(result)) {
                        return result
                    }
                }
            }
        }

        return lastError ?: ApiResult.Error.Unknown("All providers exhausted")
    }

    private fun isRetryable(error: ApiResult.Error): Boolean = when (error) {
        is ApiResult.Error.Network,
        is ApiResult.Error.Timeout,
            -> true
        is ApiResult.Error.HttpError -> error.code in RETRYABLE_HTTP_CODES
        is ApiResult.Error.Unauthorized -> false
        is ApiResult.Error.Serialization -> false
        is ApiResult.Error.Unknown -> true
    }

    private companion object {
        val RETRYABLE_HTTP_CODES = setOf(429, 500, 502, 503, 504)
    }
}
