package com.theunderseer.movementos.data.ai.telemetry

/**
 * Telemetry sink for LLM operations.
 *
 * Default: [KermitLlmTelemetry] logs structured events.
 * Future: ship to backend via Phase 4 analytics endpoint.
 */
interface LlmTelemetry {
    fun emit(event: LlmEvent)
}
