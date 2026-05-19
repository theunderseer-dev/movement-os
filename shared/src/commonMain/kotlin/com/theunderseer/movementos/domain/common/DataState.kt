package com.theunderseer.movementos.domain.common

/**
 * Wraps repository emissions with explicit state.
 *
 * - [Loading] — fetching from any source; UI shows progress indicator
 * - [Success] — fresh data delivered
 * - [Error] — operation failed; may still carry stale [data] for graceful degradation
 *
 * Repositories ALWAYS emit through this — never null-as-state, never raw values.
 */
sealed class DataState<out T> {
    data class Loading<T>(
        val cached: T? = null,
    ) : DataState<T>()

    data class Success<T>(
        val data: T,
    ) : DataState<T>()

    data class Error<T>(
        val error: DataError,
        val cached: T? = null,
    ) : DataState<T>()
}
