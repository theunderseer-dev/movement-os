package com.theunderseer.movementos.backend.plugins

import com.theunderseer.movementos.backend.routing.healthRoutes
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        healthRoutes()
        // Future: authRoutes(), programRoutes(), syncRoutes(), llmProxyRoutes()
    }
}
