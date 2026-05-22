# ADR-010: Multiplatform Testing Strategy

## Context
Shared module owns business logic. Its test suite should be the most thorough and
fastest in the project. Without explicit strategy, tests drift to Android-coupled,
slow, flaky implementations.

## Decision
- **kotlin.test** as assertion library (multiplatform, no JUnit Android dependency in shared)
- **kotlinx-coroutines-test** with `runTest` for suspending tests
- **Turbine** for Flow emission assertions
- **Kover** for coverage with 80% threshold on `domain.usecase` package
- **No MockK** — fakes preferred over mocks (see "Fakes over mocks" section)

## On rejecting MockK

MockK multiplatform was considered and explicitly rejected at this stage:

- **Repositories** use fakes (`FakeProgramRepository`) that mimic real behavior with
  `MutableStateFlow` + in-memory storage. Tests verify outcomes, not call sequences.
- **External collaborators** (LLM client, analytics) use single-method stubs via
  anonymous object or `fun interface` lambda. No mocking framework needed.
- **Refactor resilience** — mocks couple tests to call sequences via `verify { }`
  assertions; renaming a method or changing call order breaks tests even when
  behavior is correct. Fakes only break on actual behavior changes.
- **KMM compatibility** — MockK multiplatform support is incomplete and adds
  configuration overhead for native targets (iOS) without proportional value.

If a future test genuinely needs strict call-order verification (e.g. analytics events
emitted in specific sequence), MockK can be added on demand to that specific module's
`testImplementation`. Until then, the project stays mock-free.

## Test taxonomy

| Type | Location | Example |
|------|----------|---------|
| Pure logic (use cases) | `shared/commonTest` | AdaptDifficultyUseCaseTest |
| Local DB integration | `shared/androidUnitTest` | DefaultProgramRepositoryTest with JdbcSqliteDriver |
| Network with MockEngine | `shared/commonTest` | SafeApiCallTest |
| UI composable | `feature/*/androidUnitTest` | OnboardingScreenTest with Compose tester |
| Full graph | `androidApp/androidTest` (later) | happy path smoke |

## Fixtures-first

All test entity construction goes through `core/testing/fixtures`. Inline
`Program(id = ..., name = ..., ...)` in test bodies forbidden. It obscures which
fields the test actually depends on.

## Fakes over mocks

Default to writing fakes (real-behavior implementations of interfaces). Mocks reserved
for one-shot collaborators (analytics, loggers). Fakes don't break on refactors that
change call sequences but preserve behavior.

## Deterministic everything

- `Clock` injected, never `Clock.System.now()` directly
- Dispatchers injected via `TestDispatcherProvider`
- Random seeded via test-only seed
- No `Thread.sleep`, no `delay` in test setup

## Trade-offs
- Kover-on-shared adds ~5s per build (acceptable for confidence gain)
- `androidUnitTest` for SQLDelight integration tests means iOS tests don't cover full path
  — accepted; same Kotlin code runs on both, JVM integration test gives sufficient signal
- Fixture defaults can hide intent if overused (every test passes defaults). Convention
  is to override fields the test cares about explicitly

## Consequences
- `./gradlew :shared:test` runs in <30s end-to-end
- Adding a new use case = new fixture + 3-5 tests (typically <50 lines)
- CI gate: Kover threshold fails build on coverage regression below 80%
- New contributors read `docs/testing.md` as onboarding
