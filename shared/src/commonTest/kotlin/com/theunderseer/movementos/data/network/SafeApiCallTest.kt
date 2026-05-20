package com.theunderseer.movementos.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SafeApiCallTest {
    private fun clientWithResponse(
        status: HttpStatusCode,
        body: String = "",
    ) = HttpClient(
        MockEngine { _ ->
            respond(
                content = ByteReadChannel(body),
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        },
    ) {
        expectSuccess = true
        install(ContentNegotiation) { json() }
    }

    @Test
    fun `5xx response maps to HttpError`() =
        runTest {
            val client = clientWithResponse(HttpStatusCode.InternalServerError)
            val result = safeApiCall { client.get("/test") }
            assertIs<ApiResult.Error.HttpError>(result)
            assertEquals(500, result.code)
        }

    @Test
    fun `401 maps to Unauthorized`() =
        runTest {
            val client = clientWithResponse(HttpStatusCode.Unauthorized)
            val result = safeApiCall { client.get("/test") }
            assertIs<ApiResult.Error.Unauthorized>(result)
        }

    @Test
    fun `200 maps to Success`() =
        runTest {
            val client = clientWithResponse(HttpStatusCode.OK, "ok")
            val result = safeApiCall { client.get("/test") }
            assertIs<ApiResult.Success<HttpResponse>>(result)
        }
}
