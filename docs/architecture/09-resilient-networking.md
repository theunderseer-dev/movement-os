# ADR-009: Resilient Remote Networking Layer

## Context
Mobile networks fail constantly: packet loss, dropped connections, captive portals,
expired tokens. Without a unified layer, every data source reinvents retry, error
mapping, and auth logic.

## Decision
Single Ktor [HttpClient] in commonMain, configured via [NetworkConfig]. Platform engines
swap via expect/actual: CIO on Android (Kotlin-native, no OkHttp), Darwin on iOS
(uses NSURLSession).

## Pipeline
1. **ContentNegotiation** — kotlinx.serialization JSON with `ignoreUnknownKeys` for
   forward compatibility with API additions
2. **HttpTimeout** — connect 10s / request 30s / socket 30s (mobile-tuned)
3. **BearerAuthPlugin** — custom plugin (not Ktor Auth) for full control over refresh flow
4. **HttpRequestRetry** — exponential backoff, 3 attempts, transient failures only
5. **Logging** — LogLevel.BODY in debug, NONE in release (Kermit bridge)

## ApiResult vs exceptions
`safeApiCall { }` wraps Ktor exceptions to typed [ApiResult]. Repositories never
catch exceptions, they `when` on result. Sealed hierarchy:
- Success<T>
- Error: Network, Timeout, Unauthorized, HttpError(code), Serialization, Unknown

UI sees [DataError] after repository maps from ApiResult. No Ktor types leak past
data layer.

## Auth refresh strategy
Single retry on 401:
1. Read refresh token from storage
2. Call refresh handler (Phase 4 endpoint)
3. On success: save new tokens, retry original request
4. On failure: clear tokens, propagate 401 (UI shows login)

No infinite refresh loops. Revoked refresh tokens fail once and exit gracefully.

## Trade-offs
- CIO engine on Android (not OkHttp): smaller APK, fewer transitive dependencies,
  but loses OkHttp ecosystem (interceptors, caching). Acceptable since Ktor plugins
  cover same ground.
- `expectSuccess = true` enables exception-based flow, required for plugin-driven
  retry/auth. Without it, Ktor returns even 5xx as Success<HttpResponse>.
- Single HttpClient instance: shared connection pool, auth state. Multiple clients
  would duplicate state and waste TCP connections.

## Consequences
- Repositories: no try/catch, no retry logic, no auth code. All in network layer
- Adding a new endpoint: define DTO + add method to remote data source, ~5 lines
- LLM integration (Phase 3) reuses entire pipeline — same client, same retry, same auth
- Backend deployment (Phase 4) plugs into BaseUrl via NetworkConfig (no client changes)
