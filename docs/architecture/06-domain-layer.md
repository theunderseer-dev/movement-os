# ADR-006: Domain Layer Architecture

## Context
Business logic mixed with framework code (Android Context, Ktor classes, Compose)
is untestable, untouchable across platforms, and accumulates accidental dependencies.

## Decision
Domain layer lives in `shared/src/commonMain` with zero infrastructure imports:
- **Models:** immutable data classes with value types for distinct concepts
- **Repositories:** interfaces only, no implementations
- **Use cases:** single-purpose orchestrators with `operator fun invoke()`

Repository implementations and use case wiring live in `data/` layer.

## Patterns

### Value types
`@JvmInline value class Duration(val seconds: Int)` over raw `Int`. Compile-time
prevention of accidentally passing exercise count where duration expected.

### Strategy injection
`GenerateProgramUseCase` takes `ProgramGenerator` strategy. Same use case orchestrates
AI generation and rule-based fallback. Strategy swaps; use case stays.

### Result wrapping
Use cases that can fail return `Result<T>`. Callers handle success/failure explicitly.
No leaked exceptions.

### Clock injection
Time-dependent use cases inject `Clock`. Tests use fixed clock for deterministic timestamps.

## Trade-offs
- Coroutine `Flow` in repository interfaces requires `kotlinx.coroutines` in commonMain.
    Acceptable since it's a multiplatform library, not Android-specific.
- `kotlin.time.Instant` (with @OptIn(ExperimentalTime::class) global) — Kotlin's
  canonical time API since 2.1. Will graduate from experimental in upcoming releases.

## Consequences
- Domain layer compiles to JVM, iOS, JS — same business rules across platforms
- Use cases testable without Android/iOS runtime
- Repositories can swap implementations (memory cache, SQLDelight, remote) without
  domain knowing
- Future: domain test coverage target ≥ 80% (enforced via Kover threshold)
