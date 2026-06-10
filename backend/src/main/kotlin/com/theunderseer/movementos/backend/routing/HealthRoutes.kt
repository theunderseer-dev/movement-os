package com.theunderseer.movementos.backend.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable

/**
 * Liveness and readiness endpoints.
 *
 * /health — liveness: process is up (used by orchestrator to restart crashed pods)
 * /ready  — readiness: dependencies (DB) reachable (used to gate traffic)
 *
 * Excluded from request logging (Monitoring filter) to avoid log spam.
 */
fun Route.healthRoutes() {
    get("/health") {
        call.respond(HttpStatusCode.OK, HealthStatus(status = "UP"))
    }

    get("/ready") {
        // Phase 4 issue 2: check DB connectivity here
        call.respond(HttpStatusCode.OK, HealthStatus(status = "READY"))
    }
}

@Serializable
private data class HealthStatus(
    val status: String,
)
