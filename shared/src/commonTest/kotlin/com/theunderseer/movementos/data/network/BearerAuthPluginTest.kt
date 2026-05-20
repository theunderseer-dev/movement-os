package com.theunderseer.movementos.data.network

import com.theunderseer.movementos.data.network.auth.AuthRefreshHandler
import com.theunderseer.movementos.data.network.auth.BearerAuthPlugin
import com.theunderseer.movementos.data.network.auth.InMemoryAuthTokenStorage
import com.theunderseer.movementos.data.network.auth.RefreshResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BearerAuthPluginTest {
    @Test
    fun `401 triggers token refresh and retries request`() =
        runTest {
            var callCount = 0
            val tokenStorage =
                InMemoryAuthTokenStorage().apply {
                    saveTokens("expired-token", "refresh-token")
                }
            val refreshHandler =
                object : AuthRefreshHandler {
                    override suspend fun refresh(refreshToken: String): RefreshResult =
                        RefreshResult.Success("new-token", "new-refresh-token")
                }

            val engine =
                MockEngine { request ->
                    callCount++
                    val token = request.headers[HttpHeaders.Authorization]
                    when {
                        token == "Bearer expired-token" -> respond("", HttpStatusCode.Unauthorized)
                        token == "Bearer new-token" -> respond("ok", HttpStatusCode.OK)
                        else -> respond("", HttpStatusCode.BadRequest)
                    }
                }

            val client =
                HttpClient(engine) {
                    install(BearerAuthPlugin) {
                        this.tokenStorage = tokenStorage
                        this.refreshHandler = refreshHandler
                    }
                }

            val response = client.get("/test")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(2, callCount)
            assertEquals("new-token", tokenStorage.getAccessToken())
        }
}
