package com.theunderseer.movementos.data.network.auth

/**
 * Attaches Bearer token to outgoing requests and refreshes on 401.
 *
 * Strategy:
 * 1. Before request: read current access token, attach to Authorization header
 * 2. After response: if 401, fetch refresh token, call refresh handler
 * 3. On refresh success: save new tokens, retry original request once
 * 4. On refresh failure: clear tokens, propagate 401 (UI navigates to login)
 *
 * Single retry. No infinite loops on persistent 401 (revoked refresh token).
 */
class BearerAuthPluginConfig {
    lateinit var tokenStorage: AuthTokenStorage
    lateinit var refreshHandler: AuthRefreshHandler
}
