package com.theunderseer.movementos.core.testing.ai

import com.theunderseer.movementos.data.ai.telemetry.LlmEvent
import com.theunderseer.movementos.data.ai.telemetry.LlmTelemetry

/**
 * Telemetry fake capturing all emitted events for assertions.
 *
 * ```
 * assertEquals(1, telemetry.eventsOf<LlmEvent.CacheHit>().size)
 * assertEquals(2, telemetry.eventsOf<LlmEvent.RequestSucceeded>().size)
 * ```
 */
class FakeLlmTelemetry : LlmTelemetry {
    private val capturedEvents = mutableListOf<LlmEvent>()

    override fun emit(event: LlmEvent) {
        capturedEvents.add(event)
    }

    val events: List<LlmEvent> get() = capturedEvents.toList()

    inline fun <reified T : LlmEvent> eventsOf(): List<T> = events.filterIsInstance<T>()

    fun reset() {
        capturedEvents.clear()
    }
}
