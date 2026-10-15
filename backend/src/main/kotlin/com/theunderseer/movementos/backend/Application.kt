package com.theunderseer.movementos.backend

import com.theunderseer.movementos.backend.config.AppConfig
import com.theunderseer.movementos.backend.di.appModule
import com.theunderseer.movementos.backend.observability.configureMonitoring
import com.theunderseer.movementos.backend.plugins.configureDefaultHeaders
import com.theunderseer.movementos.backend.plugins.configureRouting
import com.theunderseer.movementos.backend.plugins.configureSerialization
import com.theunderseer.movementos.backend.plugins.configureStatusPages
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.koin.core.context.startKoin
import org.koin.logger.slf4jLogger

/**
 * Backend entry point.
 *
 * Loads config, starts Koin, boots Netty on configured host/port.
 * Plugin installation order matters: monitoring (request id) first so all
 * subsequent logs carry correlation id; status pages before routing so errors
 * from routes are caught.
 */
fun main() {
    val koin =
        startKoin {
            slf4jLogger()
            modules(appModule)
        }.koin

    val config = koin.get<AppConfig>()

    embeddedServer(
        factory = Netty,
        port = config.server.port,
        host = config.server.host,
        module = { module() },
    ).start(wait = true)
}

fun Application.module() {
    configureMonitoring()
    configureDefaultHeaders()
    configureSerialization()
    configureStatusPages()
    configureRouting()
}
