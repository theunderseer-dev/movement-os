package com.theunderseer.movementos.data.ai.budget

import com.theunderseer.movementos.data.ai.LlmProvider

internal class TokenBudgetExceededException(
    val provider: LlmProvider,
    val remainingTokens: Int,
) : RuntimeException("Token budget exceeded for $provider (remaining: $remainingTokens)")
