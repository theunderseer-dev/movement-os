# ADR-004: CI Pipeline and Quality Gates

## Context
Pet projects often skip CI entirely or treat it as ceremony. Without enforced
quality gates, lint violations and broken tests sneak into main, and the repo
becomes harder to maintain over time.

## Decision
Two-stage CI pipeline triggered on every PR and push to main:

1. **Static analysis** (ktlint + Detekt) — fails on style violations
2. **Build & test** — fails on broken build or failing unit tests

Pre-commit hook runs the same checks locally to catch issues before push,
keeping CI noise low.

## Quality gates
- ktlint: enforced formatting, no warnings allowed
- Detekt: enforced complexity, naming, style rules with project-specific config
- Unit tests: must pass; reports uploaded as artifacts on failure
- Branch protection: main requires both checks green before merge

## Trade-offs accepted
- Pre-commit adds ~10-20s per commit (worth it for caught issues)
- Strict Detekt config will catch real issues but also requires occasional rule
  tuning per project area (e.g., Compose composables)

## Consequences
- Main branch always in green state
- Code style is uniform without manual review
- Professional infrastructure from first commit
- Test failures visible in PR checks immediately

## References
- ktlint: https://pinterest.github.io/ktlint/
- Detekt: https://detekt.dev/
- Now in Android sample CI: https://github.com/android/nowinandroid/blob/main/.github/workflows/Build.yaml