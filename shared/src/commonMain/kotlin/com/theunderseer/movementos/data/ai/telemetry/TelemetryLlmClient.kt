package com.theunderseer.movementos.data.ai.telemetry

import com.theunderseer.movementos.data.ai.LlmClient
import com.theunderseer.movementos.data.ai.LlmProvider
import com.theunderseer.movementos.data.ai.LlmRequest
import com.theunderseer.movementos.data.ai.LlmResponse
import com.theunderseer.movementos.data.network.ApiResult
import kotlin.time.Clock
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

/**
 * Decorator emitting telemetry events for every LLM operation.
 *
 * Wraps another [LlmClient] and instruments timing, success/failure, provider used.
 * Composes with other decorators (cache, budget, rate-limit). Order matters
 * (telemetry should be outermost to observe everything).
 */
@OptIn(ExperimentalTime::class)
internal class TelemetryLlmClient(
    private val delegate: LlmClient,
    private val telemetry: LlmTelemetry,
    private val clock: Clock = Clock.System,
) : LlmClient {
    override suspend fun complete(request: LlmRequest): ApiResult<LlmResponse> {
        val requestId = generateRequestId()
        val startedAt = clock.now()

        telemetry.emit(
            LlmEvent.RequestStarted(
                timestamp = startedAt,
                requestId = requestId,
                provider = LlmProvider.GEMINI, // default (orchestrator may override)
                promptLength = (request.systemPrompt?.length ?: 0) + request.userPrompt.length,
                maxTokens = request.maxTokens,
                temperature = request.temperature,
            ),
        )

        val result = delegate.complete(request)
        val endedAt = clock.now()
        val duration =
            (endedAt.toEpochMilliseconds() - startedAt.toEpochMilliseconds())
                .toDuration(DurationUnit.MILLISECONDS)

        when (result) {
            is ApiResult.Success -> {
                telemetry.emit(
                    LlmEvent.RequestSucceeded(
                        timestamp = endedAt,
                        requestId = requestId,
                        provider = result.data.provider,
                        model = result.data.model,
                        usage = result.data.usage,
                        duration = duration,
                        fromCache = false, // cache decorator marks this separately
                    ),
                )
            }

            is ApiResult.Error -> {
                telemetry.emit(
                    LlmEvent.RequestFailed(
                        timestamp = endedAt,
                        requestId = requestId,
                        provider = null,
                        errorType = result::class.simpleName ?: "Unknown",
                        duration = duration,
                    ),
                )
            }
        }

        return result
    }

    private fun generateRequestId(): String =
        "req-${clock.now().toEpochMilliseconds()}-" +
            "${(0..MAX_REQUEST_ID_SUFFIX).random()}"

    private companion object {
        const val MAX_REQUEST_ID_SUFFIX = 9999
    }
}
