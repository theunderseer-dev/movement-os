package com.theunderseer.movementos.data.repository

import com.theunderseer.movementos.data.local.ProgramLocalDataSource
import com.theunderseer.movementos.data.orchestration.NetworkBoundResourceConfig
import com.theunderseer.movementos.data.orchestration.RemoteNotFoundException
import com.theunderseer.movementos.data.orchestration.RemoteUnavailableException
import com.theunderseer.movementos.data.orchestration.RepositoryOrchestration
import com.theunderseer.movementos.data.orchestration.networkBoundResource
import com.theunderseer.movementos.data.remote.ProgramRemoteDataSource
import com.theunderseer.movementos.domain.common.DataError
import com.theunderseer.movementos.domain.common.DataState
import com.theunderseer.movementos.domain.model.Program
import com.theunderseer.movementos.domain.repository.ProgramRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

private const val SYNC_TABLE = "Programs"

/**
 * Reactive repository: DB is source of truth, network refreshes on staleness.
 *
 * UI subscribes to [observeActiveProgram] and receives:
 * - Loading (with cached data, if any) immediately
 * - Success once DB emits
 * - Error with cached fallback if network refresh fails
 *
 * Errors are logged but never thrown to subscribers — graceful degradation.
 */
internal class DefaultProgramRepository(
    private val local: ProgramLocalDataSource,
    private val remote: ProgramRemoteDataSource,
    private val orchestration: RepositoryOrchestration,
) : ProgramRepository {
    override fun observeActiveProgram(forceRefresh: Boolean): Flow<DataState<Program>> =
        networkBoundResource(
            config =
                NetworkBoundResourceConfig(
                    loadFromLocal = { local.observeActive() },
                    shouldFetch = { cached ->
                        forceRefresh ||
                            cached == null ||
                            orchestration.staleChecker.isStale(
                                orchestration.syncMetadata.getLastSyncedAt(SYNC_TABLE),
                            )
                    },
                    fetchRemote = {
                        orchestration.logger.d { "Fetching active program from remote" }
                        remote.getActive() ?: throw RemoteNotFoundException("active program")
                    },
                    saveRemoteResult = { fetched ->
                        local.save(fetched)
                        orchestration.syncMetadata.markSynced(SYNC_TABLE)
                        orchestration.logger.d { "Active program refreshed and persisted" }
                    },
                    errorMapper = ::mapError,
                ),
            dispatcher = orchestration.dispatcher,
        )

    override suspend fun getById(id: String): Program? =
        withContext(orchestration.dispatcher) {
            local.getById(id)
        }

    override suspend fun save(program: Program) =
        withContext(orchestration.dispatcher) {
            local.save(program)
            // Remote push deferred to background sync (Phase 7)
        }

    override suspend fun deactivate(id: String) =
        withContext(orchestration.dispatcher) {
            local.deactivate(id)
        }

    private fun mapError(throwable: Throwable): DataError {
        orchestration.logger.w(throwable) { "Remote fetch failed for active program" }
        return when (throwable) {
            is RemoteNotFoundException -> DataError.NotFound
            is RemoteUnavailableException -> DataError.Network
            else -> DataError.Unknown(throwable.message)
        }
    }
}
