package com.theunderseer.movementos.data.repository

import com.theunderseer.movementos.data.local.GoalLocalDataSource
import com.theunderseer.movementos.data.orchestration.NetworkBoundResourceConfig
import com.theunderseer.movementos.data.orchestration.RemoteNotFoundException
import com.theunderseer.movementos.data.orchestration.RemoteUnavailableException
import com.theunderseer.movementos.data.orchestration.RepositoryOrchestration
import com.theunderseer.movementos.data.orchestration.networkBoundResource
import com.theunderseer.movementos.data.remote.GoalRemoteDataSource
import com.theunderseer.movementos.domain.common.DataError
import com.theunderseer.movementos.domain.common.DataState
import com.theunderseer.movementos.domain.model.UserGoal
import com.theunderseer.movementos.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

private const val SYNC_TABLE = "Goals"

internal class DefaultGoalRepository(
    private val local: GoalLocalDataSource,
    private val remote: GoalRemoteDataSource,
    private val orchestration: RepositoryOrchestration,
) : GoalRepository {
    override fun observeCurrentGoal(forceRefresh: Boolean): Flow<DataState<UserGoal?>> =
        networkBoundResource(
            config =
                NetworkBoundResourceConfig(
                    loadFromLocal = { local.observeCurrent() },
                    shouldFetch = { cached ->
                        forceRefresh ||
                            cached == null ||
                            orchestration.staleChecker.isStale(
                                orchestration.syncMetadata.getLastSyncedAt(SYNC_TABLE),
                            )
                    },
                    fetchRemote = {
                        orchestration.logger.d { "Fetching current goal from remote" }
                        remote.getCurrent() ?: throw RemoteNotFoundException("current goal")
                    },
                    saveRemoteResult = { fetched ->
                        local.save(fetched)
                        orchestration.syncMetadata.markSynced(SYNC_TABLE)
                        orchestration.logger.d { "Current goal refreshed and persisted" }
                    },
                    errorMapper = ::mapError,
                ),
            dispatcher = orchestration.dispatcher,
        )

    override suspend fun getById(id: String): UserGoal? =
        withContext(orchestration.dispatcher) {
            local.getById(id)
        }

    override suspend fun save(goal: UserGoal) =
        withContext(orchestration.dispatcher) {
            local.save(goal)
        }

    private fun mapError(throwable: Throwable): DataError {
        orchestration.logger.w(throwable) { "Remote fetch failed for current goal" }
        return when (throwable) {
            is RemoteNotFoundException -> DataError.NotFound
            is RemoteUnavailableException -> DataError.Network
            else -> DataError.Unknown(throwable.message)
        }
    }
}
