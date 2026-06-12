package com.theunderseer.movementos.backend

import com.theunderseer.movementos.backend.common.ApiError
import com.theunderseer.movementos.backend.common.exceptions.AppException
import com.theunderseer.movementos.backend.common.exceptions.ErrorCode
import com.theunderseer.movementos.backend.plugins.configureSerialization
import com.theunderseer.movementos.backend.plugins.configureStatusPages
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class StatusPagesTest {
    @Test
    fun `maps AppException to structured ApiError`() =
        testApplication {
            application {
                configureSerialization()
                configureStatusPages()
                routing {
                    get("/boom") { throw AppException.NotFoundException("Program not found") }
                }
            }
            val client =
                createClient {
                    this@testApplication.install(ContentNegotiation) {
                        json(
                            Json {
                                ignoreUnknownKeys = true
                            },
                        )
                    }
                }

            val response = client.get("/boom")

            assertEquals(HttpStatusCode.NotFound, response.status)
            val error = response.body<ApiError>()
            assertEquals(ErrorCode.NOT_FOUND, error.code)
            assertEquals("Program not found", error.message)
        }

    @Test
    fun `maps unhandled exception to INTERNAL_ERROR without leaking details`() =
        testApplication {
            application {
                configureSerialization()
                configureStatusPages()
                routing {
                    get("/crash") { throw IllegalStateException("internal DB connection string leaked") }
                }
            }
            val client =
                createClient {
                    this@testApplication.install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
                }

            val response = client.get("/crash")

            assertEquals(HttpStatusCode.InternalServerError, response.status)
            val error = response.body<ApiError>()
            assertEquals(ErrorCode.INTERNAL_ERROR, error.code)
            assertEquals("An unexpected error occurred", error.message) // generic, no leak
        }
}
