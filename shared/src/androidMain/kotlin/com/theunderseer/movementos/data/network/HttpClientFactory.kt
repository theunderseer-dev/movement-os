@file:JvmName("HttpClientFactoryAndroid")

package com.theunderseer.movementos.data.network

import com.theunderseer.movementos.data.network.auth.AuthRefreshHandler
import com.theunderseer.movementos.data.network.auth.AuthTokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

actual fun createHttpClient(
    config: NetworkConfig,
    tokenStorage: AuthTokenStorage,
    refreshHandler: AuthRefreshHandler,
): HttpClient =
    HttpClient(CIO) {
        applyCommonConfig(config, tokenStorage, refreshHandler)
    }
