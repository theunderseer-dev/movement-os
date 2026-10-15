package com.theunderseer.movementos.backend.observability

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.CallLogging
import org.slf4j.event.Level
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Correlation ID per request, propagated to logs via MDC.
 *
 * Reads incoming X-Request-Id header or generates one. Every log line for a
 * request carries the same id — essential for tracing in production.
 */
@OptIn(ExperimentalUuidApi::class)
fun Application.configureMonitoring() {
    install(CallId) {
        header("X-Request-Id")
        generate { Uuid.random().toString() }
        verify { it.isNotBlank() }
    }
    install(CallLogging) {
        level = Level.INFO
        callIdMdc("requestId")
        filter { call ->
            !call.request.local.uri
                .startsWith("/health")
        }
    }
}
