package com.theunderseer.movementos.data.repository

import co.touchlab.kermit.Logger
import com.theunderseer.movementos.data.local.GoalLocalDataSource
import com.theunderseer.movementos.data.local.SyncMetadataLocalDataSource
import com.theunderseer.movementos.data.orchestration.StaleChecker
import com.theunderseer.movementos.data.orchestration.networkBoundResource
import com.theunderseer.movementos.data.remote.GoalRemoteDataSource
import com.theunderseer.movementos.domain.common.DataError
import com.theunderseer.movementos.domain.common.DataState
import com.theunderseer.movementos.domain.model.UserGoal
import com.theunderseer.movementos.domain.repository.GoalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

private const val SYNC_TABLE = "Goals"

internal class DefaultGoalRepository(
    private val local: GoalLocalDataSource,
    private val remote: GoalRemoteDataSource,
    private val syncMetadata: SyncMetadataLocalDataSource,
    private val staleChecker: StaleChecker,
    private val dispatcher: CoroutineDispatcher,
    private val logger: Logger = Logger.withTag("GoalRepository"),
) : GoalRepository {
    override fun observeCurrentGoal(forceRefresh: Boolean): Flow<DataState<UserGoal?>> =
        networkBoundResource(
            loadFromLocal = { local.observeCurrent() },
            shouldFetch = { cached ->
                forceRefresh ||
                    cached == null ||
                    staleChecker.isStale(
                        syncMetadata.getLastSyncedAt(SYNC_TABLE),
                    )
            },
            fetchRemote = {
                logger.d { "Fetching current goal from remote" }
                remote.getCurrent()
            },
            saveRemoteResult = { fetched ->
                if (fetched != null) local.save(fetched)
                syncMetadata.markSynced(SYNC_TABLE)
            },
            errorMapper = ::mapError,
            dispatcher = dispatcher,
        )

    override suspend fun getById(id: String): UserGoal? =
        withContext(dispatcher) {
            local.getById(id)
        }

    override suspend fun save(goal: UserGoal) =
        withContext(dispatcher) {
            local.save(goal)
        }

    private fun mapError(throwable: Throwable): DataError {
        logger.w(throwable) { "Remote fetch failed for current goal" }
        return DataError.Unknown(throwable.message)
    }
}
