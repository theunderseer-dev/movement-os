# ADR-011: Provider-Agnostic LLM Orchestration

## Context
The product depends on LLM-generated content (movement programs, adaptive coaching).
Single-provider lock-in is risky: rate limits, pricing changes, deprecations, regional
availability. Each provider has different request/response schemas, auth mechanisms,
and error formats.

## Decision
**Adapter pattern with unified contracts:**

- `LlmRequest` / `LlmResponse` / `LlmError` — provider-agnostic domain types
- `LlmAdapter` — per-provider implementation translating to/from native API
- `LlmOrchestrator` — implements `LlmClient` facade; routes through adapters with retry + fallback
- `ProviderHealthTracker` — in-session cooldown for failing providers

Domain code (use cases) depends only on `LlmClient` facade. Adapters are internal to data layer.

## Provider selection strategy

`LlmRequestStrategy` decides provider order per request:

- Default: preferred order (Gemini → Anthropic → OpenAI), healthy providers first
- Future: route by request type (JSON mode prefers specific providers, long context to high-capacity models)

## Retry classification

Retryable (try next provider): 429 (rate limit), 5xx, Network, Timeout
Non-retryable (fail fast): 401, 4xx (other), Serialization errors

## API key management

- Local: `local.properties` (gitignored), exposed via `BuildConfig.*_API_KEY`
- CI: GitHub Secrets
- Production: secure backend proxy in Phase 4 (no keys in mobile binary)

## Trade-offs
- **Three adapters to maintain** instead of one because resilience > simplicity
- **Token usage normalization** — each provider reports differently; unified `TokenUsage` is approximate
- **No streaming yet** — `complete()` returns full response; streaming added when UI needs it
- **In-memory health tracking** — resets on app restart; persistent tracking deferred until needed

## Consequences
- Domain code never imports `OpenAi*` / `Anthropic*` / `Gemini*` types but only `LlmRequest/Response`
- Adding a new provider = new adapter + register in DI; no orchestrator changes
- Phase 4 backend proxy replaces direct adapter calls without domain code changes
- Test coverage focuses on orchestrator logic (retry, fallback, health tracking); adapters tested with MockEngine
