package com.theunderseer.movementos.core.testing.ai

import com.theunderseer.movementos.data.ai.LlmProvider
import com.theunderseer.movementos.data.ai.LlmRequest
import com.theunderseer.movementos.data.ai.LlmResponse
import com.theunderseer.movementos.data.ai.adapter.LlmAdapter
import com.theunderseer.movementos.data.network.ApiResult

/**
 * Fake adapter for testing orchestrator-level behavior.
 *
 * Each adapter can be configured independently — useful for testing fallback
 * chains (e.g., Gemini fails, Anthropic succeeds).
 *
 * ```
 * val gemini = FakeLlmAdapter(LlmProvider.GEMINI).respondWithError(ApiResult.Error.Network)
 * val anthropic = FakeLlmAdapter(LlmProvider.ANTHROPIC).respondWithSuccess("...")
 * val orchestrator = LlmOrchestrator(adapters = mapOf(GEMINI to gemini, ANTHROPIC to anthropic), ...)
 * ```
 */
class FakeLlmAdapter(
    override val provider: LlmProvider,
) : LlmAdapter {
    private val responses = mutableListOf<ApiResult<LlmResponse>>()
    private val capturedRequests = mutableListOf<LlmRequest>()
    private var defaultResponse: ApiResult<LlmResponse>? = null

    fun respondWith(response: ApiResult<LlmResponse>): FakeLlmAdapter =
        apply {
            responses.add(response)
        }

    fun respondWithSuccess(content: String): FakeLlmAdapter = respondWith(FakeLlmClient.successResponse(content, provider))

    fun respondWithError(error: ApiResult.Error): FakeLlmAdapter = respondWith(error)

    fun alwaysFail(error: ApiResult.Error): FakeLlmAdapter =
        apply {
            defaultResponse = error
        }

    fun alwaysSucceed(content: String): FakeLlmAdapter =
        apply {
            defaultResponse = FakeLlmClient.successResponse(content, provider)
        }

    override suspend fun complete(request: LlmRequest): ApiResult<LlmResponse> {
        capturedRequests.add(request)
        return when {
            responses.isNotEmpty() -> responses.removeAt(0)
            defaultResponse != null -> defaultResponse!!
            else -> ApiResult.Error.Unknown("Fake adapter not configured")
        }
    }

    val callCount: Int get() = capturedRequests.size
    val requests: List<LlmRequest> get() = capturedRequests.toList()
}
