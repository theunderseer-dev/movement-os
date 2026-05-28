package com.theunderseer.movementos.data.ai.integration

import com.theunderseer.movementos.core.testing.ai.FakeLlmClient
import com.theunderseer.movementos.core.testing.ai.FakeLlmResponseCache
import com.theunderseer.movementos.core.testing.ai.FakeLlmTelemetry
import com.theunderseer.movementos.core.testing.ai.fixtures.ProgramGenerationFixtures
import com.theunderseer.movementos.data.ai.LlmRequest
import com.theunderseer.movementos.data.ai.cache.CachedLlmClient
import com.theunderseer.movementos.data.ai.telemetry.LlmEvent
import com.theunderseer.movementos.data.network.ApiResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class CacheIntegrationTest {
    private val testRequest = LlmRequest(systemPrompt = "system", userPrompt = "generate")
    private val cache = FakeLlmResponseCache()
    private val telemetry = FakeLlmTelemetry()

    @Test
    fun `first request goes through delegate, subsequent hits cache`() =
        runTest {
            val delegate =
                FakeLlmClient().respondWithSuccess(ProgramGenerationFixtures.validProgramJson)
            val cached = CachedLlmClient(delegate = delegate, cache = cache, telemetry = telemetry)

            val first = cached.complete(testRequest)
            val second = cached.complete(testRequest)

            assertIs<ApiResult.Success<*>>(first)
            assertIs<ApiResult.Success<*>>(second)
            assertEquals(1, delegate.requestCount)
            assertEquals(1, cache.putCount)
            assertEquals(1, cache.hitCount)
            assertEquals(1, telemetry.eventsOf<LlmEvent.CacheHit>().size)
        }

    @Test
    fun `failed responses are not cached`() =
        runTest {
            val delegate =
                FakeLlmClient()
                    .respondWithError(ApiResult.Error.Network)
                    .setDefault(ApiResult.Error.Network)
            val cached = CachedLlmClient(delegate = delegate, cache = cache, telemetry = telemetry)

            cached.complete(testRequest)
            cached.complete(testRequest)

            assertEquals(2, delegate.requestCount)
            assertEquals(0, cache.putCount)
        }

    @Test
    fun `different requests get different cache entries`() =
        runTest {
            val delegate =
                FakeLlmClient()
                    .respondWithSuccess("response 1")
                    .respondWithSuccess("response 2")
            val cached = CachedLlmClient(delegate = delegate, cache = cache, telemetry = telemetry)

            cached.complete(LlmRequest(systemPrompt = null, userPrompt = "prompt A"))
            cached.complete(LlmRequest(systemPrompt = null, userPrompt = "prompt B"))

            assertEquals(2, delegate.requestCount)
            assertEquals(2, cache.putCount)
            assertEquals(2, cache.keys().size)
        }
}
