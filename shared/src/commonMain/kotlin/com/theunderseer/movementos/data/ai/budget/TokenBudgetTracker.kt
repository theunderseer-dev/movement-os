package com.theunderseer.movementos.data.ai.budget

import com.theunderseer.movementos.data.ai.LlmProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Tracks per-provider token consumption against [TokenBudget].
 *
 * In-memory implementation. Resets on app restart. For production, persist
 * via SQLDelight (Phase 4 with backend). Sufficient for portfolio demo.
 */
@OptIn(ExperimentalTime::class)
class TokenBudgetTracker(
    private val budgets: Map<LlmProvider, TokenBudget>,
    private val clock: Clock = Clock.System,
) {
    private val state = mutableMapOf<LlmProvider, ProviderUsage>()
    private val mutex = Mutex()

    /**
     * Reserves tokens from provider's budget. Returns true if within budget.
     *
     * Call BEFORE making the LLM request. Pessimistic reservation prevents
     * over-spending when multiple concurrent requests check budget simultaneously.
     */
    suspend fun tryReserve(
        provider: LlmProvider,
        estimatedTokens: Int,
    ): Boolean =
        mutex.withLock {
            val budget = budgets[provider] ?: return@withLock true // no budget = unrestricted
            val usage = currentUsage(provider, budget)
            if (usage.consumedTokens + estimatedTokens > budget.maxTokens) {
                false
            } else {
                state[provider] = usage.copy(consumedTokens = usage.consumedTokens + estimatedTokens)
                true
            }
        }

    /**
     * Reconciles reservation with actual usage after request completes.
     *
     * If actual < estimated → refund difference. If actual > estimated → no-op
     * (already counted; small over-budget overruns acceptable).
     */
    suspend fun reconcile(
        provider: LlmProvider,
        estimatedTokens: Int,
        actualTokens: Int,
    ) = mutex.withLock {
        val budget = budgets[provider] ?: return@withLock
        val usage = state[provider] ?: return@withLock
        val delta = actualTokens - estimatedTokens
        if (delta < 0) {
            state[provider] = usage.copy(consumedTokens = (usage.consumedTokens + delta).coerceAtLeast(0))
        }
    }

    suspend fun remaining(provider: LlmProvider): Int =
        mutex.withLock {
            val budget = budgets[provider] ?: return@withLock Int.MAX_VALUE
            val usage = currentUsage(provider, budget)
            (budget.maxTokens - usage.consumedTokens).coerceAtLeast(0)
        }

    private fun currentUsage(
        provider: LlmProvider,
        budget: TokenBudget,
    ): ProviderUsage {
        val now = clock.now()
        val existing = state[provider]
        return if (existing == null || now > existing.periodEnd) {
            ProviderUsage(
                consumedTokens = 0,
                periodStart = now,
                periodEnd = now.plus(budget.periodDuration),
            )
        } else {
            existing
        }
    }

    private data class ProviderUsage(
        val consumedTokens: Int,
        val periodStart: Instant,
        val periodEnd: Instant,
    )
}
