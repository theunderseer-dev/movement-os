package com.theunderseer.movementos.core.testing.time

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Deterministic clock for tests.
 *
 * Use when test asserts on exact timestamps:
 * ```
 * val clock = FixedClock(Instant.fromEpochMilliseconds(1_700_000_000_000))
 * val useCase = RecordSessionUseCase(repo, clock)
 * ```
 */
@OptIn(ExperimentalTime::class)
class FixedClock(
    private val instant: Instant,
) : Clock {
    override fun now(): Instant = instant

    companion object {
        val DEFAULT_INSTANT: Instant = Instant.fromEpochMilliseconds(1_700_000_000_000)

        fun default(): FixedClock = FixedClock(DEFAULT_INSTANT)
    }
}
