# Testing Conventions

## Test pyramid

| Layer | Module | Coverage target | Speed |
|-------|--------|-----------------|-------|
| Domain (use cases) | shared/commonTest | ≥80% | <100ms each |
| Data (repositories) | shared/androidUnitTest | ≥70% | <500ms each |
| UI (composables) | feature/*/androidUnitTest | smoke tests only | <1s each |
| Integration (full graph) | androidApp | happy paths only | <5s each |

`./gradlew :shared:test` runs in <30s. CI gate enforces coverage threshold via Kover.

## Naming conventions

Test names read as **behavior specs** in backtick-quoted strings:

✅ Good:
- `fun \`increases time per session when last three sessions all too easy\`()`
- `fun \`emits Error with cached data when network fails\`()`
- `fun \`returns null when no active program exists\`()`

❌ Bad:
- `fun testAdaptDifficulty()`
- `fun test1()`
- `fun adaptDifficultyShouldWorkCorrectly()`

Pattern: **`{action/state} when {precondition}`** or **`{outcome} on {trigger}`**.

## When to mock vs fake

| Use | When |
|-----|------|
| **Fake** (real-behavior impl) | Repository, data source, anything with state across calls. Real example: `FakeProgramRepository` stores programs in `MutableMap` and emits via `MutableStateFlow`. |
| **Mock** (MockK) | External interface called once, where you only care about the call, not the result. Example: analytics tracker, logger. |
| **Stub** (hardcoded responses) | One-off return values for a single test. Inline lambda or anonymous object. |
| **Real impl** | Pure functions, value types, mappers. No mocking needed — just call them. |

**Default to fakes.** Mocks couple tests to call sequences — refactors break tests even when behavior is correct.

## Fixtures

All test entities created via fixtures in `core/testing`:
```kotlin
val program = TestPrograms.aProgram(name = "Custom plan")
val goal = TestGoals.aGoal(focus = MovementType.BACK_PAIN_RELIEF)
```

Override only fields the test cares about (defaults handle the rest). Avoid inline data class construction in tests, it obscures intent (which fields matter to this test?).

## Deterministic time

Never use `Clock.System.now()` directly in domain code. Inject `Clock` interface, default to `Clock.System` in production, override with `FixedClock` or `MutableClock` in tests.

```kotlin
// Production code
class RecordSessionUseCase(
    private val repo: SessionRepository,
    private val clock: Clock = Clock.System,
)

// Test
val clock = FixedClock(Instant.fromEpochMilliseconds(1_700_000_000_000))
val useCase = RecordSessionUseCase(repo, clock)
```

## Flow testing

Use [Turbine](https://github.com/cashapp/turbine):

```kotlin
@Test
fun `observeActiveProgram emits null initially then saved program`() = runTest {
    repository.observeActiveProgram().test {
        assertNull(awaitItem())

        repository.save(testProgram)
        assertEquals(testProgram.id, awaitItem()?.id)

        cancelAndIgnoreRemainingEvents()
    }
}
```

Rules:
- Always `cancelAndIgnoreRemainingEvents()` at the end (StateFlow doesn't complete)
- Use `awaitItem()`, not `expectNoEvents()` — explicit assertions, no implicit waits
- One `.test {}` block per logical flow assertion

## Dispatchers

Use `UnconfinedTestDispatcher` for use case tests (coroutines run immediately).
Use `StandardTestDispatcher` when test needs explicit `advanceTimeBy()` calls (TTL, retry, debounce).

Both come from `kotlinx-coroutines-test` and are wired through `TestDispatcherProvider`.

## No real dependencies in tests

Tests must NOT:
- Hit real network → use `MockEngine` for Ktor, `FakeRemoteDataSource` for higher layers
- Touch real DB → use `JdbcSqliteDriver(IN_MEMORY)` in integration tests
- Use real clock → inject `Clock` interface
- Depend on test order → each `@Test` independent, no shared mutable state
- Run on real Android → use `androidUnitTest` (JVM), not `androidTest` (instrumented)

If a test needs Android Context, it's an integration test, not unit test. Put it in `androidUnitTest` with Robolectric, or skip it.
