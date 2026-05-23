package com.theunderseer.movementos.data.ai.orchestrator

import com.theunderseer.movementos.data.ai.LlmProvider
import com.theunderseer.movementos.data.ai.LlmRequest

/**
 * Selects providers to try, in order, for a given request.
 *
 * Default strategy: prefer healthy providers, fall back to fallback order.
 * Custom strategies can route based on request properties (e.g. JSON mode
 * preferring specific providers, large prompts going to high-context models).
 */
fun interface LlmRequestStrategy {
    fun selectProviders(
        request: LlmRequest,
        availableProviders: Set<LlmProvider>,
        healthTracker: ProviderHealthTracker,
    ): List<LlmProvider>
}

/**
 * Default strategy: healthy providers in declared order, then unhealthy as last resort.
 */
class DefaultLlmRequestStrategy(
    private val preferredOrder: List<LlmProvider> = LlmProvider.entries,
) : LlmRequestStrategy {
    override fun selectProviders(
        request: LlmRequest,
        availableProviders: Set<LlmProvider>,
        healthTracker: ProviderHealthTracker,
    ): List<LlmProvider> {
        val available = preferredOrder.filter { it in availableProviders }
        val healthy = available.filter { healthTracker.isHealthy(it) }
        val unhealthy = available.filterNot { healthTracker.isHealthy(it) }
        return healthy + unhealthy
    }
}
