# ADR-001: Module Dependency Rules

## Context
Multi-module Android projects fail when dependencies become spaghetti. We need explicit rules.

## Decision
Dependencies flow in one direction:
app → feature/* → core/* → shared

## Rules
- core:common has no internal dependencies
- core:designsystem depends only on Compose
- core:ui depends on core:designsystem, core:common
- core:testing depends on core:common
- feature/* depend on core/*, shared (never on other features)
- app depends on all features and core

## Consequences
- Adding a new feature requires no changes to other features
- Refactoring core/* is safe (compile-time enforced)
- Features stay independently testable