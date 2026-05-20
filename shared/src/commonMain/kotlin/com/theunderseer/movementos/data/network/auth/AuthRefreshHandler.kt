package com.theunderseer.movementos.data.network.auth

/**
 * Refreshes expired access tokens.
 *
 * Called by [BearerAuthPlugin] on 401. Real impl hits /auth/refresh endpoint.
 *
 */
interface AuthRefreshHandler {
    suspend fun refresh(refreshToken: String): RefreshResult
}

sealed class RefreshResult {
    data class Success(
        val accessToken: String,
        val refreshToken: String,
    ) : RefreshResult()

    data object Failure : RefreshResult()
}

class StubAuthRefreshHandler : AuthRefreshHandler {
    override suspend fun refresh(refreshToken: String): RefreshResult = RefreshResult.Failure
}
