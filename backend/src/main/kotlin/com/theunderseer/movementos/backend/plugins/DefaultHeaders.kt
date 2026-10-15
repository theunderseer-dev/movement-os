package com.theunderseer.movementos.backend.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.defaultheaders.DefaultHeaders

fun Application.configureDefaultHeaders() {
    install(DefaultHeaders) {
        header("X-Engine", "MovementOS")
    }
}
