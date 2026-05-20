package com.theunderseer.movementos.data.network.auth

/**
 * Persists authentication tokens.
 *
 * Multiplatform abstraction.
 * Real impl uses platform secure storage:
 * Android EncryptedSharedPreferences, iOS Keychain.
 *
 * Implementation deferred to Phase 4 (when backend lands).
 */
interface AuthTokenStorage {
    suspend fun getAccessToken(): String?

    suspend fun getRefreshToken(): String?

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
    )

    suspend fun clearTokens()
}

/** In-memory implementation for development. Replace with secure storage in Phase 4. */
class InMemoryAuthTokenStorage : AuthTokenStorage {
    private var accessToken: String? = null
    private var refreshToken: String? = null

    override suspend fun getAccessToken(): String? = accessToken

    override suspend fun getRefreshToken(): String? = refreshToken

    override suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
    ) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    override suspend fun clearTokens() {
        accessToken = null
        refreshToken = null
    }
}
