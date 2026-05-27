package com.theunderseer.movementos.data.ai.telemetry

import co.touchlab.kermit.Logger

/**
 * Logs telemetry events as structured Kermit messages.
 *
 * Format: `[LlmTelemetry] <EventType> <key=value pairs>`. Greppable in logs,
 * future-compatible with log aggregators (Datadog, ELK).
 */
class KermitLlmTelemetry(
    private val logger: Logger = Logger.withTag("LlmTelemetry"),
) : LlmTelemetry {
    override fun emit(event: LlmEvent) {
        val message =
            when (event) {
                is LlmEvent.RequestStarted -> "RequestStarted requestId=${event.requestId} provider=${event.provider} promptLen=${event.promptLength} maxTokens=${event.maxTokens}"
                is LlmEvent.RequestSucceeded -> "RequestSucceeded requestId=${event.requestId} provider=${event.provider} model=${event.model} totalTokens=${event.usage.totalTokens} duration=${event.duration} fromCache=${event.fromCache}"
                is LlmEvent.RequestFailed -> "RequestFailed requestId=${event.requestId} provider=${event.provider} errorType=${event.errorType} duration=${event.duration}"
                is LlmEvent.FallbackTriggered -> "FallbackTriggered requestId=${event.requestId} originalError=${event.originalError} fallbackType=${event.fallbackType}"
                is LlmEvent.CacheHit -> "CacheHit requestId=${event.requestId} cacheKey=${event.cacheKey}"
            }
        logger.i { message }
    }
}
