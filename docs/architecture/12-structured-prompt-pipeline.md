# ADR-012: Structured Prompt and Response Pipeline

## Context
LLM responses are non-deterministic by default. Same prompt can return different
JSON shapes, malformed structures, prose mixed with data. Without structured
pipeline, every consumer reinvents parsing, validation, error handling.

## Decision
**Three-layer pipeline:**

1. **PromptTemplate** — immutable templates with named placeholders (`{{name}}`)
2. **PromptBuilder + PromptConfig** — uniform LlmRequest construction (temperature, max_tokens, response_format)
3. **ResponseParser + ResponseValidationError** — JSON extraction (markdown stripping, brace matching), kotlinx.serialization parse, typed errors

## Schema-in-prompt strategy
JSON schema embedded directly in system prompt. LLM has explicit contract; output
validates against schema at parse time. No external schema files. Schema lives
where it's used.

## Deterministic IDs
LLM-generated programs get IDs derived from `goal.id + generatedAt.toEpochMilliseconds()`.
Retries on same goal don't accumulate duplicate Programs in DB. ID format:
`program-{goalId}-{epochMs}`, `{programId}-session-{index}`, `{sessionId}-exercise-{index}`.

## Fallback chain
`GenerateProgramUseCase` injects both `LlmProgramGenerator` (primary) and
`DeterministicProgramGenerator` (fallback). On LLM failure or invalid response,
fallback generates rule-based program. User never sees "AI unavailable" error.

## Response repair (optional retry)
`ResponseRepairStrategy` asks LLM to fix malformed JSON in one follow-up call.
Single retry only, no recursion. Useful for syntax errors (trailing commas,
missing quotes); semantic errors (wrong enum) skip repair and fall back.

## Trade-offs
- **Schema duplication** between Kotlin types and prompt strings — accepted because
  schema-in-prompt is single source of truth for LLM; @Serializable types mirror it
- **Temperature 0.3 for JSON** — low enough for stability, high enough to avoid
  templated outputs; not 0 because LLMs occasionally return null at temperature=0
- **No JSON schema validation library** (e.g., everit, networknt) — kotlinx.serialization
    + custom semantic mapping is sufficient at current scale; revisit if schema grows
- **Repair single-shot only** — multi-retry repair amplifies cost without proportional success
- **Deterministic IDs leak generation timestamp** — acceptable trade-off for idempotence

## Consequences
- Adding a new prompt type = new `PromptTemplate` + `@Serializable` response DTO + mapper
- Domain `ProgramGenerator` interface unchanged, implementation swaps freely
- Fallback always works, degraded mode never breaks UX
- Phase 3 issue 3 (observability) attaches to existing PromptBuilder/ResponseParser
  via decorator pattern; no rewrite needed
