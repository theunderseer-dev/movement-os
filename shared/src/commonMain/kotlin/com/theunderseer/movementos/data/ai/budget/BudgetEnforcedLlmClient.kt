package com.theunderseer.movementos.data.ai.budget

import co.touchlab.kermit.Logger
import com.theunderseer.movementos.data.ai.LlmClient
import com.theunderseer.movementos.data.ai.LlmProvider
import com.theunderseer.movementos.data.ai.LlmRequest
import com.theunderseer.movementos.data.ai.LlmResponse
import com.theunderseer.movementos.data.network.ApiResult

/**
 * Decorator enforcing token budgets before delegating to the underlying client.
 *
 * Strategy:
 * 1. Estimate tokens for request (heuristic: chars / 4)
 * 2. Try reserve from default provider's budget
 * 3. If reservation fails, return Unauthorized error (acts as fallback signal to use cached/deterministic response)
 * 4. After actual response, reconcile actual vs estimated usage
 *
 * For multi-provider orchestrator, this is applied AT the orchestrator level —
 * the orchestrator can try next provider when budget exhausted.
 */
internal class BudgetEnforcedLlmClient(
    private val delegate: LlmClient,
    private val tracker: TokenBudgetTracker,
    private val defaultProvider: LlmProvider,
    private val logger: Logger = Logger.withTag("BudgetEnforced"),
) : LlmClient {
    override suspend fun complete(request: LlmRequest): ApiResult<LlmResponse> {
        val estimatedTokens = estimateTokens(request)

        if (!tracker.tryReserve(defaultProvider, estimatedTokens)) {
            val remaining = tracker.remaining(defaultProvider)
            logger.w { "Budget exhausted for $defaultProvider, remaining=$remaining" }
            return ApiResult.Error.Unknown("Token budget exceeded for $defaultProvider")
        }

        val result = delegate.complete(request)
        if (result is ApiResult.Success) {
            tracker.reconcile(
                provider = result.data.provider,
                estimatedTokens = estimatedTokens,
                actualTokens = result.data.usage.totalTokens,
            )
        }
        return result
    }

    private fun estimateTokens(request: LlmRequest): Int {
        val promptChars = (request.systemPrompt?.length ?: 0) + request.userPrompt.length
        return (promptChars / CHARS_PER_TOKEN) + request.maxTokens
    }

    private companion object {
        const val CHARS_PER_TOKEN = 4 // industry heuristic for English text
    }
}
