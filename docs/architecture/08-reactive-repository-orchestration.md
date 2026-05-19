# ADR-008: Reactive Repository Orchestration

## Context
UI needs unified subscription that delivers data regardless of source (cache, disk, network).
Without orchestration, UI handles three signals (loading, success, error) via ad-hoc null
checks and try/catch, leading to inconsistent UX and untestable code.

## Decision
Repositories return `Flow<DataState<T>>` where `DataState` is a sealed class with explicit
Loading/Success/Error states. Local DB is the single source of truth. Network is a side
effect that updates the DB.

## NetworkBoundResource pattern
Adapted from Google's NIA and the classic Architecture Components sample:

1. Emit `Loading(cached)` immediately if cache exists
2. Read latest from local data source
3. Trigger remote fetch when `shouldFetch` (TTL-based or force)
4. On success: persist to DB → local Flow emits → UI sees Success
5. On failure: emit `Error(cached)` → UI shows error indicator over stale data

## TTL strategy
Per-entity staleness:
- Programs: 1 hour (rarely changes)
- Sessions: 24 hours (semi-static)
- Goals: 6 hours (rarely changes)

## Graceful degradation
Network failure NEVER throws to subscriber. Logged via Kermit, emitted as
`DataState.Error` with cached fallback. UI decides whether to show banner.

## Trade-offs
- `DataState` adds wrapper boilerplate vs direct `Flow<T>` (explicit state worth it for UI clarity)
- Cached fallback in Error duplicates data (small memory cost, big UX win)
- TTL hardcoded in DI (easy to adjust per environment)

## Consequences
- Single subscription pattern in UI: `repo.observeActiveProgram().collectAsState()`
- Repository tests cover happy path + 3 degraded scenarios (cache miss, network fail, force refresh)
- Background sync uses same DataSource layer, no repository changes needed
- No Ktor/SQLDelight types leak. DataError abstracts platform-specific errors
