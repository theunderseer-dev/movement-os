package com.theunderseer.movementos.data.network.auth

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode

val BearerAuthPlugin =
    createClientPlugin("BearerAuth", ::BearerAuthPluginConfig) {
        val tokenStorage = pluginConfig.tokenStorage
        val refreshHandler = pluginConfig.refreshHandler

        onRequest { request, _ ->
            val token = tokenStorage.getAccessToken() ?: return@onRequest
            request.headers.append(HttpHeaders.Authorization, "Bearer $token")
        }

        on(io.ktor.client.plugins.api.Send) { request ->
            var call = proceed(request)
            if (call.response.status == HttpStatusCode.Unauthorized) {
                val refreshToken = tokenStorage.getRefreshToken()
                if (refreshToken != null) {
                    when (val result = refreshHandler.refresh(refreshToken)) {
                        is RefreshResult.Success -> {
                            tokenStorage.saveTokens(result.accessToken, result.refreshToken)
                            request.headers[HttpHeaders.Authorization] = "Bearer ${result.accessToken}"
                            call = proceed(request)
                        }
                        RefreshResult.Failure -> {
                            tokenStorage.clearTokens()
                        }
                    }
                }
            }
            call
        }
    }
