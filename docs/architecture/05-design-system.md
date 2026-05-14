# ADR-005: Compose Design System Foundation

## Context
Without a design system, feature modules accumulate inconsistencies like different button styles, raw hex colors, magic-number padding. So refactoring becomes painful.

## Decision
All design tokens live in core/designsystem. Feature modules consume them via:
- `MaterialTheme.colorScheme.*` — colors
- `MaterialTheme.typography.*` — type
- `MaterialTheme.shapes.*` — corners
- `LocalSpacing.current.*` — spacing

No feature module references raw `Color(0xFF...)`, raw dp values, or custom `TextStyle`.

## Token strategy
- Colors: semantic naming via Material 3 ColorScheme (primary, surface, etc.) — not "blue" or "yellow"
- Typography: standard MD3 scale (display/headline/title/body/label)
- Spacing: 4dp-based scale (xs=4, sm=8, md=12, lg=16, xl=24, xxl=32)
- Shapes: MD3 5-step scale (extraSmall to extraLarge)

## Light/Dark
System-driven via `isSystemInDarkTheme()`. No manual toggle in v1.

## Trade-offs
- LocalSpacing CompositionLocal: marginally slower than direct dp, but enforces token usage at compile boundary
- Skipped dynamic color (Material You). Brand-first identity matters more than personalization for this product

## Consequences
- Theme changes require touching only core/designsystem files
- Component previews render correctly across both themes via @ThemePreviews
- Linter rule could be added later: ban raw Color() and dp values outside core/designsystem