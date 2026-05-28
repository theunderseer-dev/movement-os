package com.theunderseer.movementos.data.ai.integration

import com.theunderseer.movementos.core.testing.ai.FakeLlmAdapter
import com.theunderseer.movementos.core.testing.ai.fixtures.ProgramGenerationFixtures
import com.theunderseer.movementos.core.testing.ai.scenarios.LlmFailureScenarios
import com.theunderseer.movementos.data.ai.LlmProvider
import com.theunderseer.movementos.data.ai.LlmRequest
import com.theunderseer.movementos.data.ai.LlmResponse
import com.theunderseer.movementos.data.ai.orchestrator.DefaultLlmRequestStrategy
import com.theunderseer.movementos.data.ai.orchestrator.LlmOrchestrator
import com.theunderseer.movementos.data.ai.orchestrator.ProviderHealthTracker
import com.theunderseer.movementos.data.network.ApiResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class LlmFailureRecoveryTest {
    private val testRequest =
        LlmRequest(
            systemPrompt = "system",
            userPrompt = "test",
        )

    @Test
    fun `falls back to Anthropic when Gemini rate limited`() =
        runTest {
            val gemini = FakeLlmAdapter(LlmProvider.GEMINI).respondWithError(LlmFailureScenarios.rateLimited)
            val anthropic =
                FakeLlmAdapter(LlmProvider.ANTHROPIC).respondWithSuccess(
                    ProgramGenerationFixtures.validProgramJson,
                )

            val orchestrator =
                LlmOrchestrator(
                    adapters = mapOf(LlmProvider.GEMINI to gemini, LlmProvider.ANTHROPIC to anthropic),
                    strategy = DefaultLlmRequestStrategy(),
                    healthTracker = ProviderHealthTracker(),
                )

            val result = orchestrator.complete(testRequest)

            assertIs<ApiResult.Success<LlmResponse>>(result)
            assertEquals(LlmProvider.ANTHROPIC, result.data.provider)
            assertEquals(1, gemini.callCount)
            assertEquals(1, anthropic.callCount)
        }

    @Test
    fun `tries all providers on network failures`() =
        runTest {
            val gemini = FakeLlmAdapter(LlmProvider.GEMINI).respondWithError(LlmFailureScenarios.networkFailure)
            val anthropic = FakeLlmAdapter(LlmProvider.ANTHROPIC).respondWithError(LlmFailureScenarios.timeout)
            val openai =
                FakeLlmAdapter(LlmProvider.OPENAI).respondWithSuccess(
                    ProgramGenerationFixtures.validProgramJson,
                )

            val orchestrator =
                LlmOrchestrator(
                    adapters =
                        mapOf(
                            LlmProvider.GEMINI to gemini,
                            LlmProvider.ANTHROPIC to anthropic,
                            LlmProvider.OPENAI to openai,
                        ),
                    strategy = DefaultLlmRequestStrategy(),
                    healthTracker = ProviderHealthTracker(),
                )

            val result = orchestrator.complete(testRequest)

            assertIs<ApiResult.Success<LlmResponse>>(result)
            assertEquals(LlmProvider.OPENAI, result.data.provider)
            assertEquals(1, gemini.callCount)
            assertEquals(1, anthropic.callCount)
            assertEquals(1, openai.callCount)
        }

    @Test
    fun `returns last error when all providers fail`() =
        runTest {
            val gemini = FakeLlmAdapter(LlmProvider.GEMINI).respondWithError(LlmFailureScenarios.networkFailure)
            val anthropic = FakeLlmAdapter(LlmProvider.ANTHROPIC).respondWithError(LlmFailureScenarios.timeout)

            val orchestrator =
                LlmOrchestrator(
                    adapters = mapOf(LlmProvider.GEMINI to gemini, LlmProvider.ANTHROPIC to anthropic),
                    strategy = DefaultLlmRequestStrategy(),
                    healthTracker = ProviderHealthTracker(),
                )

            val result = orchestrator.complete(testRequest)

            assertIs<ApiResult.Error.Timeout>(result)
        }

    @Test
    fun `does not retry on unauthorized error`() =
        runTest {
            val gemini = FakeLlmAdapter(LlmProvider.GEMINI).respondWithError(LlmFailureScenarios.unauthorized)
            val anthropic =
                FakeLlmAdapter(LlmProvider.ANTHROPIC).alwaysSucceed(
                    ProgramGenerationFixtures.validProgramJson,
                )

            val orchestrator =
                LlmOrchestrator(
                    adapters = mapOf(LlmProvider.GEMINI to gemini, LlmProvider.ANTHROPIC to anthropic),
                    strategy = DefaultLlmRequestStrategy(),
                    healthTracker = ProviderHealthTracker(),
                )

            val result = orchestrator.complete(testRequest)

            assertIs<ApiResult.Error.Unauthorized>(result)
            assertEquals(0, anthropic.callCount)
        }
}
