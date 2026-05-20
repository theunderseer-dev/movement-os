package com.theunderseer.movementos.data.network

import com.theunderseer.movementos.data.network.auth.AuthTokenStorage
import com.theunderseer.movementos.data.network.auth.BearerAuthPlugin
import com.theunderseer.movementos.data.network.retry.installRetryPolicy
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import co.touchlab.kermit.Logger as KermitLogger

/**
 * Factory for the shared Ktor [HttpClient].
 *
 * Single instance per app.
 * Reuses connection pool, auth state, retry counters.
 * Platform engines (CIO/Darwin) wired via [createHttpClient] expect/actual.
 */
expect fun createHttpClient(
    config: NetworkConfig,
    tokenStorage: AuthTokenStorage,
    refreshHandler: com.theunderseer.movementos.data.network.auth.AuthRefreshHandler,
): HttpClient

internal fun HttpClientConfig<*>.applyCommonConfig(
    config: NetworkConfig,
    tokenStorage: AuthTokenStorage,
    refreshHandler: com.theunderseer.movementos.data.network.auth.AuthRefreshHandler,
) {
    expectSuccess = true // throws on non-2xx; exceptions mapped to ApiResult in safeApiCall

    install(ContentNegotiation) {
        json(jsonConfig)
    }

    install(HttpTimeout) {
        connectTimeoutMillis = config.connectTimeout.inWholeMilliseconds
        requestTimeoutMillis = config.requestTimeout.inWholeMilliseconds
        socketTimeoutMillis = config.socketTimeout.inWholeMilliseconds
    }

    install(Logging) {
        level = if (config.isDebug) LogLevel.BODY else LogLevel.NONE
        logger =
            object : Logger {
                private val log = KermitLogger.withTag("HttpClient")

                override fun log(message: String) = log.d { message }
            }
    }

    install(BearerAuthPlugin) {
        this.tokenStorage = tokenStorage
        this.refreshHandler = refreshHandler
    }

    installRetryPolicy()

    defaultRequest {
        contentType(ContentType.Application.Json)
        url { takeFrom(URLBuilder(config.baseUrl)) }
    }
}

internal val jsonConfig: Json =
    Json {
        ignoreUnknownKeys = true // forward-compatible with API additions
        isLenient = true
        explicitNulls = false
        encodeDefaults = true
    }
