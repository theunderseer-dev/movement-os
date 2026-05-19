package com.theunderseer.movementos.data.orchestration

import com.theunderseer.movementos.domain.common.DataError
import com.theunderseer.movementos.domain.common.DataState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Generic Network-bound Resource flow.
 *
 * Strategy:
 * 1. Emit Loading with cached data if any
 * 2. Read latest from local DB
 * 3. If [shouldFetch] returns true (stale or empty), call [fetchRemote]
 * 4. On success, [saveRemoteResult] persists to DB
 * 5. On failure, emit Error with cached data (silent fallback)
 * 6. DB Flow continues streaming — UI updates reactively from local writes
 *
 * Local DB is the single source of truth. UI only observes [loadFromLocal].
 * Network calls are side effects that update the DB.
 * @param dispatcher background dispatcher for I/O
 */
internal fun <Local, Remote> networkBoundResource(
    config: NetworkBoundResourceConfig<Local, Remote>,
    dispatcher: CoroutineDispatcher,
): Flow<DataState<Local>> =
    flow {
        val cached = config.loadFromLocal().first()
        emit(DataState.Loading(cached))

        val shouldRefresh = runCatching { config.shouldFetch(cached) }.getOrDefault(false)

        if (shouldRefresh) {
            runCatching { config.fetchRemote() }
                .onSuccess { remote -> config.saveRemoteResult(remote) }
                .onFailure { throwable ->
                    emit(DataState.Error(config.errorMapper(throwable), cached))
                }
        }

        emitAll(
            config
                .loadFromLocal()
                .map { local ->
                    if (local != null) {
                        DataState.Success(local)
                    } else {
                        DataState.Error<Local>(DataError.NotFound, cached = null)
                    }
                }.catch { emit(DataState.Error(DataError.Local, cached)) },
        )
    }.flowOn(dispatcher)
