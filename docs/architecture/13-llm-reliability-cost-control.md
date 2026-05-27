# ADR-013: LLM Reliability and Cost-Control Mechanisms

## Context
LLM APIs cost money per token. Free tiers have hard rate limits (Gemini 15 req/min,
Anthropic 5 req/min, OpenAI 3 req/min). Without controls, a single bug can exhaust
daily budget in minutes. Without caching, identical prompts pay full cost every time.
Without observability, cost drift is invisible until the bill arrives.

## Decision
**Decorator chain over `LlmClient`** — each concern is a separate decorator,
composed in DI:
→ TelemetryLlmClient
→ CachedLlmClient
→ BudgetEnforcedLlmClient
→ RateLimitedLlmClient
→ LlmOrchestrator (existing)

Decorators are unaware of each other. Single responsibility, easy to test,
easy to swap (e.g., disable cache in tests).

## Multi-layer cache

L1 (in-memory) → L2 (SQLDelight). Both keyed by deterministic request hash.
- L1: 50 entries, LRU eviction, instant access
- L2: persistent, 24h TTL, survives app restart
- Promotion: L2 hit copies to L1 for future requests

Cache key derived from prompt + temperature + max_tokens + response_format.
Same effective request = same key regardless of provider chosen by orchestrator.

## Token budget

Per-provider budgets in `TokenBudgetTracker` with pessimistic reservation:
- Reserve estimated tokens BEFORE request (prevents concurrent overspend)
- Reconcile actual usage after response (refund unused)
- 24h sliding window, in-memory (sufficient for v1, persistent in Phase 4)

Default budgets reflect free-tier limits:
- Gemini: 1.5M tokens/day (~30k requests at 50 tokens each)
- Anthropic: 100k tokens/day (~5 requests on a 20k-token program generation)
- OpenAI: 100k tokens/day

## Rate limiting

Token bucket per provider, refilling continuously. `acquire()` returns false on
empty bucket; `acquireOrWait()` suspends until next token available. Configured
to match each provider's free-tier RPM.

## Telemetry

`LlmEvent` sealed class enumerates structured events:
- `RequestStarted` — provider, prompt length, params
- `RequestSucceeded` — tokens used, duration, cache flag
- `RequestFailed` — error type, duration
- `FallbackTriggered` — original error, fallback strategy
- `CacheHit` — request id, cache key

Default sink (`KermitLlmTelemetry`) logs structured messages. Future Phase 4
adds backend sink for aggregated cost monitoring.

## Trade-offs

- **Decorator order matters** — telemetry outermost (observes everything), cache
  next (avoid budget/rate-limit on hits), orchestrator innermost (provider
  selection happens after gates pass)
- **In-memory budget tracker** — resets on restart, no cross-device sync. Sufficient
  for portfolio; production needs persistent + backend-synced quotas
- **Cache key includes temperature** — same prompt at temp=0.3 vs 0.7 caches separately
  (acceptable — semantically different requests)
- **No streaming responses** — simpler caching/budget logic, ok for current use case
  (program generation completes in one shot)
- **Token estimation via chars/4 heuristic** — approximate; production uses tokenizer.
  Acceptable for budgeting where 20-30% slack is fine

## Consequences

- AI cost visible: every request logs token usage; aggregating telemetry shows
  per-day spend per provider
- Cache hit rate measurable via `CacheHit` events; high hit rate = lower cost
- Rate limit errors disappear: client throttles before provider rejects
- Budget exhaustion gracefully degrades: budget error → orchestrator tries next
  provider → if all exhausted → caller falls back to deterministic generator
- Adding new control (e.g., per-user quotas in Phase 4) = new decorator, no rewrite
