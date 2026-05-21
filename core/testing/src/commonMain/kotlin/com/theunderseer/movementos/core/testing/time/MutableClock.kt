package com.theunderseer.movementos.core.testing.time

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Mutable clock for tests that advance time (TTL expiry, retry backoff, scheduled work).
 *
 * ```
 * val clock = MutableClock(initialInstant)
 * clock.advance(2.hours)
 * ```
 */
@OptIn(ExperimentalTime::class)
class MutableClock(
    initial: Instant,
) : Clock {
    private var current: Instant = initial

    override fun now(): Instant = current

    fun advance(by: Duration) {
        current = current.plus(by)
    }

    fun set(instant: Instant) {
        current = instant
    }
}
