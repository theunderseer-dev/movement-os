# ADR-003: Dependency Injection Strategy

## Context
Hilt provides excellent Android integration but doesn't work in KMM-shared code.
Koin works on KMM but lacks Hilt's compile-time safety on Android.

## Decision
Hybrid approach:
- Hilt for Android-specific dependencies (ViewModels, Android Context-aware services)
- Koin for KMM-shared dependencies (repositories, use cases, data sources)
- Bridge: Koin started from Hilt's Application class

## Boundaries
- Android-only code: Hilt only
- Shared KMM code: Koin only
- Repository interfaces in shared: declared in Koin, accessed from Android via Koin's `inject()` or `get()` in non-ViewModel code

## Consequences
- Two DI systems to maintain (mental overhead)
- Clear boundary: if you need Android Context, you're in Hilt; otherwise Koin
- KMM-shared logic remains platform-agnostic