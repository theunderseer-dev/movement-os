package com.theunderseer.movementos.data.ai.budget

import com.theunderseer.movementos.core.testing.time.MutableClock
import com.theunderseer.movementos.data.ai.LlmProvider
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class TokenBudgetTrackerTest {
    private val clock = MutableClock(Instant.fromEpochMilliseconds(1_700_000_000_000))
    private val tracker =
        TokenBudgetTracker(
            budgets = mapOf(LlmProvider.GEMINI to TokenBudget(maxTokens = 1000)),
            clock = clock,
        )

    @Test
    fun `reserves tokens within budget`() =
        runTest {
            assertTrue(tracker.tryReserve(LlmProvider.GEMINI, 500))
            assertEquals(500, tracker.remaining(LlmProvider.GEMINI))
        }

    @Test
    fun `rejects reservation exceeding budget`() =
        runTest {
            tracker.tryReserve(LlmProvider.GEMINI, 800)
            assertFalse(tracker.tryReserve(LlmProvider.GEMINI, 300))
            assertEquals(200, tracker.remaining(LlmProvider.GEMINI))
        }

    @Test
    fun `refunds unused tokens on reconciliation`() =
        runTest {
            tracker.tryReserve(LlmProvider.GEMINI, 500)
            tracker.reconcile(LlmProvider.GEMINI, estimatedTokens = 500, actualTokens = 300)

            assertEquals(800, tracker.remaining(LlmProvider.GEMINI))
        }

    @Test
    fun `resets budget on new period`() =
        runTest {
            tracker.tryReserve(LlmProvider.GEMINI, 1000)
            assertEquals(0, tracker.remaining(LlmProvider.GEMINI))

            clock.advance(25.hours)

            assertEquals(1000, tracker.remaining(LlmProvider.GEMINI))
        }

    @Test
    fun `unlimited budget for providers not in map`() =
        runTest {
            assertTrue(tracker.tryReserve(LlmProvider.OPENAI, 1_000_000))
            assertEquals(Int.MAX_VALUE, tracker.remaining(LlmProvider.OPENAI))
        }
}
