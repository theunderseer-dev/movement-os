package com.theunderseer.movementos.data.ai.budget

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/**
 * Token budget definition.
 *
 * Per-period budget (daily by default). Exceeding fails requests with [TokenBudgetExceeded].
 * Tracked separately per provider since costs differ.
 *
 * @property maxTokens hard cap on total tokens (prompt + completion)
 * @property periodDuration window over which budget resets
 */
data class TokenBudget(
    val maxTokens: Int,
    val periodDuration: Duration = 1.days,
) {
    companion object {
        val DEFAULT_DAILY = TokenBudget(maxTokens = 100_000)
    }
}
