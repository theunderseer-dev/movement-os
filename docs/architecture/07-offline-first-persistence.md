# ADR-007: Offline-First Persistence with SQLDelight

## Context
The app must work fully without network. Users practice in places with poor
connectivity (gyms, parks, abroad). Loading spinners and "no internet" errors
break the flow of a movement session.

## Decision
Local SQLDelight database is the **source of truth** for all domain entities.
Repositories read/write to local DB synchronously; remote sync layers on top in
Phase 3 as a background process.

## Schema design
- **One table per aggregate root** (Programs, Sessions, Exercises, Goals, Progress)
- **Soft sync tracking** via `updated_at_ms` + `is_dirty` flags on sync-eligible tables
- **Foreign keys + cascade deletes** for parent-child relationships
- **Indexes on access patterns**: active flag, foreign keys, time-ordering
- **JSON for short lists** (exercise cues) — avoids extra table for read-only nested data

## Migration strategy
- All schema changes go through `.sqm` migration files (versioned 1.sqm, 2.sqm, ...)
- `verifyMigrations = true` in CI catches breaking changes at build time
- Never destructive recreate — preserves user data across app updates
- `schemaOutputDirectory` commits .db schema dumps for diff review in PRs

## Sync metadata
Separate `SyncMetadata` table records per-table sync timestamps. Sync engine
(Phase 3) queries `WHERE is_dirty = 1` to push local changes, marks clean on
ACK from server. Conflict resolution: last-write-wins by `updated_at_ms`.

## Trade-offs
- Mapper boilerplate (row ↔ domain) accepted over generated bindings — explicit
  conversion keeps domain free of SQLDelight types
- IN_MEMORY driver in tests requires JDBC dependency in commonTest — small cost
  for fast, deterministic integration tests
- `Instant` stored as `INTEGER` milliseconds, not text — faster sort/range queries

## Consequences
- Repositories return `Flow<T>` from `asFlow().mapToOneOrNull()` — UI updates
  reactively on every write
- Adding a new entity = .sqm migration + queries + mapper + data source (mechanical)
- Phase 3 sync engine reads `is_dirty` flag, no repository-level changes needed
- Schema versioning enables zero-downtime production migrations
