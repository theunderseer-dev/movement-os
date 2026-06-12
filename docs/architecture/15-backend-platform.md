# ADR-015: Ktor Backend Platform

## Context
Mobile app needs a backend for: secure LLM proxy (keep API keys off devices),
cross-device sync, and server-authoritative data. Backend lives in the same
monorepo to reuse domain contracts from `shared`.

## Decision
**Ktor server on Netty**, structured into plugins + routing + DI:

- **Config**: typed `AppConfig` from HOCON overlays + env vars. Secrets only via
  env, fail-fast on missing required values.
- **DI**: Koin, mirroring shared module's pattern. `appModule` is graph root,
  sub-modules added per feature.
- **Error handling**: `AppException` sealed hierarchy → `StatusPages` → structured
  `ApiError`. Stable `ErrorCode` enum is the client contract; internal details
  never leak.
- **Observability**: correlation id per request (`CallId` + MDC), structured
  logging via Logback. Health/ready endpoints excluded from request logs.
- **Plugin order**: monitoring → headers → serialization → status pages → routing.
  Monitoring first so all logs carry request id; status pages before routing so
  route errors are caught.

## Why monorepo + shared reuse
Backend `implementation(projects.shared)` reuses domain models, LLM contracts,
and orchestration. No duplicate `Program`/`UserGoal` definitions; mobile and
backend can't drift.

## Deployment
Multi-stage Docker (JDK build → JRE runtime). Fly.io primary (Singapore region
for SEA latency). Auto-stop machines to minimize cost at zero traffic. Health
check gates traffic.

## Trade-offs
- **Netty over CIO/Jetty** — mature, production-proven, good throughput. CIO is
  lighter but less battle-tested for server workloads.
- **HOCON over pure env vars** — structured config with overlays beats sprawling
  env var lists; secrets still env-only.
- **Koin over Kodein/manual** — consistency with shared module; compile-time DI
  (Dagger/Hilt) overkill for backend's smaller graph.
- **No GraphQL** — REST is sufficient; mobile client already speaks REST via Ktor
  client adapters.

## Consequences
- New feature = new sub-module (DI) + routes + services, plugins unchanged
- Local dev: `./gradlew :backend:run` with `application-local.conf`
- Phase 4 issue 2 adds Exposed + Hikari + Postgres on this foundation
- Error contract stable from day one so clients can rely on `ErrorCode` immediately
