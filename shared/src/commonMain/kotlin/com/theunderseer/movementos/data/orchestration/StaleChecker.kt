package com.theunderseer.movementos.data.orchestration

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * Determines if cached data is stale based on TTL.
 *
 * Per-entity TTLs:
 * - Programs: 1 hour (regenerated rarely, updated by user action)
 * - Sessions: 24 hours (semi-static)
 * - Goals: 6 hours (changes slowly)
 *
 * Trade-off: aggressive TTLs reduce network usage at cost of freshness.
 */
@OptIn(ExperimentalTime::class)
internal class StaleChecker(
    private val ttl: Duration,
    private val clock: Clock = Clock.System,
) {
    fun isStale(lastFetchedAtMs: Long?): Boolean {
        if (lastFetchedAtMs == null) return true
        val now = clock.now().toEpochMilliseconds()
        return (now - lastFetchedAtMs) > ttl.inWholeMilliseconds
    }
}
