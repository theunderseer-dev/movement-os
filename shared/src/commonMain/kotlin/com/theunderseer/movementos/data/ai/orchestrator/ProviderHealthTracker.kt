package com.theunderseer.movementos.data.ai.orchestrator

import com.theunderseer.movementos.data.ai.LlmProvider
import com.theunderseer.movementos.data.network.ApiResult
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Tracks per-provider health to skip recently-failing providers.
 *
 * Simple cooldown strategy: after a failure, mark provider unhealthy for [cooldown].
 * After cooldown elapses, treat as healthy again. Successful call resets immediately.
 *
 * Not persistent (resets on app restart). Good enough for in-session orchestration.
 */
@OptIn(ExperimentalTime::class)
class ProviderHealthTracker(
    private val cooldown: Duration = DEFAULT_COOLDOWN,
    private val clock: Clock = Clock.System,
) {
    private val unhealthySince = mutableMapOf<LlmProvider, Instant>()

    fun isHealthy(provider: LlmProvider): Boolean {
        val since = unhealthySince[provider] ?: return true
        val elapsed = clock.now() - since
        return elapsed >= cooldown
    }

    fun recordSuccess(provider: LlmProvider) {
        unhealthySince.remove(provider)
    }

    fun recordFailure(provider: LlmProvider, error: ApiResult.Error) {
        if (error is ApiResult.Error.Unauthorized) return
        unhealthySince[provider] = clock.now()
    }

    private companion object {
        val DEFAULT_COOLDOWN: Duration = 1.minutes
    }
}
