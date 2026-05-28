# ADR-014: AI Integration Testing Infrastructure

## Context
LLM integrations are non-deterministic, expensive to call live (cost + rate limit),
and unreliable in CI (network, quotas). Without test infrastructure, every test
either skips AI paths or burns real API budget.

## Decision
**Multi-layered fake testing strategy** built on top of `core/testing` module:

- **`FakeLlmClient`** — programmable test double implementing `LlmClient`.
  Sequential or default responses, captures requests for assertion.
- **`FakeLlmAdapter`** — adapter-level fake for orchestrator tests.
  Per-provider configuration enables testing fallback chains.
- **`FakeLlmResponseCache`** — in-memory cache with hit/miss/put counters.
- **`FakeLlmTelemetry`** — captures emitted events; `eventsOf<T>()` for filtering.

## Fixtures

- **`LlmResponseFixtures`** — Object Mother for `LlmResponse` construction
- **`ProgramGenerationFixtures`** — JSON payload library (valid, malformed,
  missing fields, invalid enums, markdown-wrapped)

## Scenarios

- **`LlmFailureScenarios`** — canned `ApiResult.Error` instances (rate limit,
  network, timeout, unauthorized, malformed)
- **`LlmSuccessScenarios`** — pre-configured success responses for common cases

## Integration test taxonomy

| Test class | Validates |
|------------|-----------|
| `ProgramGenerationPipelineTest` | Prompt → LLM → parse → domain mapping end-to-end |
| `LlmFailureRecoveryTest` | Orchestrator fallback behavior across providers |
| `CacheIntegrationTest` | Cache hit/miss interaction with decorator |
| `BudgetEnforcementIntegrationTest` | Budget exhaustion triggers degraded path |

All run on JVM without network/keys/quotas — fast, deterministic, CI-stable.

## Why fakes over MockEngine

Ktor's `MockEngine` exists but operates at HTTP layer. Tests would need to mock
provider-specific request schemas (OpenAI vs Anthropic vs Gemini). Fakes at
`LlmClient`/`LlmAdapter` level abstract that away. Tests express intent
("provider returns rate limit") not implementation ("HTTP 429 with body...").

## Trade-offs

- **Doesn't test wire format** — wire-level testing belongs in adapter unit tests
  with `MockEngine`. We have those separately.
- **Fakes can drift from real behavior** — mitigated by integration tests that
  exercise full pipeline including parser, not just stubbed-out happy paths.
- **No live LLM tests in CI** — accepted; manual exploratory testing covers wire
  protocol changes.

## Consequences

- AI tests run in <100ms per test (no network)
- New failure scenario = add to `LlmFailureScenarios`, write 5-line test
- New provider = add `FakeLlmAdapter(NEW_PROVIDER)` config, no infrastructure changes
- CI cost: $0 in API calls for tests
- Pipeline failures reproducible deterministically (no "intermittent" flakes)
