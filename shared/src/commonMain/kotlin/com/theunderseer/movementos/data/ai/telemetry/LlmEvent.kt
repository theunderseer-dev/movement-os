package com.theunderseer.movementos.data.ai.telemetry

import com.theunderseer.movementos.data.ai.LlmProvider
import com.theunderseer.movementos.data.ai.TokenUsage
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Telemetry events emitted from LLM operations.
 *
 * Stored locally for diagnostics; future Phase 4 ships to backend analytics.
 */
@OptIn(ExperimentalTime::class)
sealed class LlmEvent {
    abstract val timestamp: Instant
    abstract val requestId: String

    data class RequestStarted(
        override val timestamp: Instant,
        override val requestId: String,
        val provider: LlmProvider,
        val promptLength: Int,
        val maxTokens: Int,
        val temperature: Double,
    ) : LlmEvent()

    data class RequestSucceeded(
        override val timestamp: Instant,
        override val requestId: String,
        val provider: LlmProvider,
        val model: String,
        val usage: TokenUsage,
        val duration: Duration,
        val fromCache: Boolean,
    ) : LlmEvent()

    data class RequestFailed(
        override val timestamp: Instant,
        override val requestId: String,
        val provider: LlmProvider?,
        val errorType: String,
        val duration: Duration,
    ) : LlmEvent()

    data class FallbackTriggered(
        override val timestamp: Instant,
        override val requestId: String,
        val originalError: String,
        val fallbackType: String,
    ) : LlmEvent()

    data class CacheHit(
        override val timestamp: Instant,
        override val requestId: String,
        val cacheKey: String,
    ) : LlmEvent()
}
