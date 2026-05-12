# ADR-002: Gradle Convention Plugins

## Context
12 modules × 30 lines of duplicate build config = unmaintainable.
Version bumps require touching every module.

## Decision
Convention plugins in build-logic/ encapsulate shared build config:
- movementos.android.library — base Android library setup
- movementos.android.feature — library + standard feature dependencies
- movementos.android.application — Android app setup

## Boundaries
- Convention plugins set defaults; modules can override
- Module-specific config (namespace, custom deps) stays in module build files
- No business logic in convention plugins

## Consequences
- Each module: 3-5 lines instead of 25-30
- Centralized version bumps (compileSdk, JVM target)
- Inspired by Now in Android (NIA) sample