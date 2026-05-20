package com.theunderseer.movementos.data.network.retry

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpRequestRetry

/**
 * Exponential backoff retry for transient failures.
 *
 * Retries:
 * - 5xx server errors (3 attempts)
 * - Network exceptions (connection refused, DNS, timeout)
 *
 * Never retries:
 * - 4xx client errors (request itself is wrong)
 * - 401 (handled by BearerAuthPlugin separately)
 *
 * Backoff: 1s, 2s, 4s + jitter. Total max delay 7s before giving up.
 */
internal fun HttpClientConfig<*>.installRetryPolicy() {
    install(HttpRequestRetry) {
        retryOnServerErrors(maxRetries = 3)
        retryOnException(maxRetries = 3, retryOnTimeout = true)
        exponentialDelay(
            base = 2.0,
            maxDelayMs = 4_000L,
            randomizationMs = 250L,
        )
    }
}
