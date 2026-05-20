package com.theunderseer.movementos.data.network

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Centralized HTTP client configuration.
 *
 * Production base URL injected via DI for environment overrides (dev/staging/prod).
 * Timeouts tuned for mobile networks: aggressive enough to surface failures fast,
 * lenient enough to handle 3G/spotty Wi-Fi.
 */
data class NetworkConfig(
    val baseUrl: String,
    val connectTimeout: Duration = 10.seconds,
    val requestTimeout: Duration = 30.seconds,
    val socketTimeout: Duration = 30.seconds,
    val isDebug: Boolean = false,
)
