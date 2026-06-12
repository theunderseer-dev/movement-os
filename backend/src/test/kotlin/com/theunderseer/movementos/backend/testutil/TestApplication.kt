package com.theunderseer.movementos.backend.testutil

import com.theunderseer.movementos.backend.module
import io.ktor.client.HttpClient
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json

/**
 * Test harness booting the full application module in-memory.
 *
 * No real network — Ktor's testApplication runs the app on an embedded test engine.
 * Provides a JSON-configured client for asserting on responses.
 */
fun runServerTest(block: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit) =
    testApplication {
        application { module() }
        val client =
            createClient {
                this@testApplication.install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
        block(client)
    }
