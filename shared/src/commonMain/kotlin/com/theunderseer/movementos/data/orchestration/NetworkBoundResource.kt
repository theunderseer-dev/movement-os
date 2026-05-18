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
 * Local DB is the single source of truth — UI only observes [loadFromLocal].
 * Network calls are side effects that update the DB.
 *
 * @param loadFromLocal cold Flow from local data source
 * @param shouldFetch decide whether remote fetch is needed (TTL, force flag, etc.)
 * @param fetchRemote suspend fun making network call
 * @param saveRemoteResult persist remote result to DB
 * @param errorMapper map exceptions to typed [DataError]
 * @param dispatcher background dispatcher for I/O
 */
internal fun <Local, Remote> networkBoundResource(
    loadFromLocal: () -> Flow<Local?>,
    shouldFetch: suspend (Local?) -> Boolean,
    fetchRemote: suspend () -> Remote,
    saveRemoteResult: suspend (Remote) -> Unit,
    errorMapper: (Throwable) -> DataError = { mapDefaultError(it) },
    dispatcher: CoroutineDispatcher,
): Flow<DataState<Local>> =
    flow {
        val cached = loadFromLocal().first()
        emit(DataState.Loading(cached))

        val shouldRefresh =
            try {
                shouldFetch(cached)
            } catch (e: Throwable) {
                false
            }

        if (shouldRefresh) {
            runCatching { fetchRemote() }
                .onSuccess { remote -> saveRemoteResult(remote) }
                .onFailure { throwable ->
                    emit(DataState.Error(errorMapper(throwable), cached))
                }
        }

        emitAll(
            loadFromLocal()
                .map { local ->
                    if (local != null) {
                        DataState.Success(local)
                    } else {
                        DataState.Error<Local>(DataError.NotFound, cached = null)
                    }
                }.catch { emit(DataState.Error(DataError.Local, cached)) },
        )
    }.flowOn(dispatcher)

private fun mapDefaultError(throwable: Throwable): DataError =
    when (throwable) {
        // Specific Ktor/SQLDelight mapping happens at repository level if needed
        else -> DataError.Unknown(throwable.message)
    }
