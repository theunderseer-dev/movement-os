package com.theunderseer.movementos.data.ai.ratelimit

import com.theunderseer.movementos.core.testing.time.MutableClock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class TokenBucketRateLimiterTest {
    private val clock = MutableClock(Instant.fromEpochMilliseconds(0))

    @Test
    fun `starts with full bucket`() =
        runTest {
            val limiter =
                TokenBucketRateLimiter(
                    config = RateLimitConfig(maxRequests = 5, perDuration = 1.minutes),
                    clock = clock,
                )

            repeat(5) { assertTrue(limiter.acquire()) }
            assertFalse(limiter.acquire())
        }

    @Test
    fun `refills over time`() =
        runTest {
            val limiter =
                TokenBucketRateLimiter(
                    config = RateLimitConfig(maxRequests = 10, perDuration = 1.minutes),
                    clock = clock,
                )

            repeat(10) { limiter.acquire() }
            assertFalse(limiter.acquire())

            clock.advance(6.seconds) // 1 token / 6 seconds = 1 token refilled
            assertTrue(limiter.acquire())
        }
}
