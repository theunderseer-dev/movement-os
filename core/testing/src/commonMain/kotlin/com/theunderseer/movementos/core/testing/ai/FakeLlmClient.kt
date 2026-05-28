package com.theunderseer.movementos.core.testing.ai

import com.theunderseer.movementos.data.ai.LlmClient
import com.theunderseer.movementos.data.ai.LlmProvider
import com.theunderseer.movementos.data.ai.LlmRequest
import com.theunderseer.movementos.data.ai.LlmResponse
import com.theunderseer.movementos.data.ai.TokenUsage
import com.theunderseer.movementos.data.network.ApiResult

/**
 * Programmable fake for LLM testing.
 *
 * Configure responses sequentially or by request-predicate match:
 * ```
 * val client = FakeLlmClient()
 *     .respondWith(successResponse("...program JSON..."))
 *     .respondWith(ApiResult.Error.Network)
 *     .respondWith(successResponse("...retry success..."))
 *
 * client.complete(request)  // returns first
 * client.complete(request)  // returns second
 * client.complete(request)  // returns third
 * ```
 *
 * Tracks all requests for assertion:
 * ```
 * assertEquals(3, client.requestCount)
 * assertEquals("expected prompt", client.lastRequest.userPrompt)
 * ```
 */
class FakeLlmClient : LlmClient {
    private val responses = mutableListOf<ApiResult<LlmResponse>>()
    private val capturedRequests = mutableListOf<LlmRequest>()
    private var defaultResponse: ApiResult<LlmResponse> = successResponse("default response")

    fun respondWith(response: ApiResult<LlmResponse>): FakeLlmClient =
        apply {
            responses.add(response)
        }

    fun respondWithSuccess(
        content: String,
        provider: LlmProvider = LlmProvider.GEMINI,
    ): FakeLlmClient = respondWith(successResponse(content, provider))

    fun respondWithError(error: ApiResult.Error): FakeLlmClient = respondWith(error)

    fun setDefault(response: ApiResult<LlmResponse>): FakeLlmClient =
        apply {
            defaultResponse = response
        }

    override suspend fun complete(request: LlmRequest): ApiResult<LlmResponse> {
        capturedRequests.add(request)
        return if (responses.isNotEmpty()) responses.removeAt(0) else defaultResponse
    }

    val requestCount: Int get() = capturedRequests.size
    val lastRequest: LlmRequest? get() = capturedRequests.lastOrNull()
    val requests: List<LlmRequest> get() = capturedRequests.toList()

    fun reset() {
        responses.clear()
        capturedRequests.clear()
    }

    companion object {
        fun successResponse(
            content: String,
            provider: LlmProvider = LlmProvider.GEMINI,
            model: String = "test-model",
            promptTokens: Int = 100,
            completionTokens: Int = 200,
        ): ApiResult.Success<LlmResponse> =
            ApiResult.Success(
                LlmResponse(
                    content = content,
                    provider = provider,
                    model = model,
                    usage =
                        TokenUsage(
                            promptTokens = promptTokens,
                            completionTokens = completionTokens,
                            totalTokens = promptTokens + completionTokens,
                        ),
                ),
            )
    }
}
