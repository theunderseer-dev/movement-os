package com.theunderseer.movementos.data.ai.orchestrator

import com.theunderseer.movementos.data.ai.LlmProvider
import com.theunderseer.movementos.data.ai.LlmRequest
import com.theunderseer.movementos.data.ai.LlmResponse
import com.theunderseer.movementos.data.ai.TokenUsage
import com.theunderseer.movementos.data.ai.adapter.LlmAdapter
import com.theunderseer.movementos.data.network.ApiResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class LlmOrchestratorTest {
    private val healthTracker = ProviderHealthTracker()

    private fun successResponse(provider: LlmProvider) =
        LlmResponse(
            content = "result",
            provider = provider,
            model = "test-model",
            usage = TokenUsage(10, 20, 30),
        )

    private class FakeAdapter(
        override val provider: LlmProvider,
        var response: ApiResult<LlmResponse>,
        var callCount: Int = 0,
    ) : LlmAdapter {
        override suspend fun complete(request: LlmRequest): ApiResult<LlmResponse> {
            callCount++
            return response
        }
    }

    @Test
    fun `succeeds on first provider when call succeeds`() =
        runTest {
            val gemini = FakeAdapter(LlmProvider.GEMINI, ApiResult.Success(successResponse(LlmProvider.GEMINI)))
            val anthropic = FakeAdapter(LlmProvider.ANTHROPIC, ApiResult.Success(successResponse(LlmProvider.ANTHROPIC)))

            val orchestrator =
                LlmOrchestrator(
                    adapters = mapOf(LlmProvider.GEMINI to gemini, LlmProvider.ANTHROPIC to anthropic),
                    strategy = DefaultLlmRequestStrategy(),
                    healthTracker = healthTracker,
                )

            val result = orchestrator.complete(LlmRequest(systemPrompt = null, userPrompt = "test"))

            assertIs<ApiResult.Success<LlmResponse>>(result)
            assertEquals(LlmProvider.GEMINI, result.data.provider)
            assertEquals(1, gemini.callCount)
            assertEquals(0, anthropic.callCount)
        }

    @Test
    fun `falls back to next provider on rate limit`() =
        runTest {
            val gemini = FakeAdapter(LlmProvider.GEMINI, ApiResult.Error.HttpError(429, "rate limited"))
            val anthropic = FakeAdapter(LlmProvider.ANTHROPIC, ApiResult.Success(successResponse(LlmProvider.ANTHROPIC)))

            val orchestrator =
                LlmOrchestrator(
                    adapters = mapOf(LlmProvider.GEMINI to gemini, LlmProvider.ANTHROPIC to anthropic),
                    strategy = DefaultLlmRequestStrategy(),
                    healthTracker = healthTracker,
                )

            val result = orchestrator.complete(LlmRequest(systemPrompt = null, userPrompt = "test"))

            assertIs<ApiResult.Success<LlmResponse>>(result)
            assertEquals(LlmProvider.ANTHROPIC, result.data.provider)
            assertEquals(1, gemini.callCount)
            assertEquals(1, anthropic.callCount)
        }

    @Test
    fun `does not retry on 4xx non-rate-limit errors`() =
        runTest {
            val gemini = FakeAdapter(LlmProvider.GEMINI, ApiResult.Error.Unauthorized)
            val anthropic = FakeAdapter(LlmProvider.ANTHROPIC, ApiResult.Success(successResponse(LlmProvider.ANTHROPIC)))

            val orchestrator =
                LlmOrchestrator(
                    adapters = mapOf(LlmProvider.GEMINI to gemini, LlmProvider.ANTHROPIC to anthropic),
                    strategy = DefaultLlmRequestStrategy(),
                    healthTracker = healthTracker,
                )

            val result = orchestrator.complete(LlmRequest(systemPrompt = null, userPrompt = "test"))

            assertIs<ApiResult.Error.Unauthorized>(result)
            assertEquals(0, anthropic.callCount)
        }

    @Test
    fun `returns last error when all providers fail`() =
        runTest {
            val gemini = FakeAdapter(LlmProvider.GEMINI, ApiResult.Error.Network)
            val anthropic = FakeAdapter(LlmProvider.ANTHROPIC, ApiResult.Error.Timeout)

            val orchestrator =
                LlmOrchestrator(
                    adapters = mapOf(LlmProvider.GEMINI to gemini, LlmProvider.ANTHROPIC to anthropic),
                    strategy = DefaultLlmRequestStrategy(),
                    healthTracker = healthTracker,
                )

            val result = orchestrator.complete(LlmRequest(systemPrompt = null, userPrompt = "test"))

            assertIs<ApiResult.Error.Timeout>(result)
        }
}
