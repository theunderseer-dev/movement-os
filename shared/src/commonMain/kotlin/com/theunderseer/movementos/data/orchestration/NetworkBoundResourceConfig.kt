package com.theunderseer.movementos.data.orchestration

import com.theunderseer.movementos.domain.common.DataError
import kotlinx.coroutines.flow.Flow

/**
 * @param loadFromLocal cold Flow from local data source
 * @param shouldFetch decide whether remote fetch is needed (TTL, force flag, etc.)
 * @param fetchRemote suspend fun making network call
 * @param saveRemoteResult persist remote result to DB
 * @param errorMapper map exceptions to typed [DataError]
 */
internal data class NetworkBoundResourceConfig<Local, Remote>(
    val loadFromLocal: () -> Flow<Local?>,
    val shouldFetch: suspend (Local?) -> Boolean,
    val fetchRemote: suspend () -> Remote,
    val saveRemoteResult: suspend (Remote) -> Unit,
    val errorMapper: (Throwable) -> DataError = { mapDefaultError(it) },
)

private fun mapDefaultError(throwable: Throwable): DataError =
    when (throwable) {
        // Specific Ktor/SQLDelight mapping happens at repository level if needed
        else -> DataError.Unknown(throwable.message)
    }
