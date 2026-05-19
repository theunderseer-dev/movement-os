package com.theunderseer.movementos.data.repository

import com.theunderseer.movementos.data.local.ProgressLocalDataSource
import com.theunderseer.movementos.data.local.SessionLocalDataSource
import com.theunderseer.movementos.data.orchestration.NetworkBoundResourceConfig
import com.theunderseer.movementos.data.orchestration.RemoteNotFoundException
import com.theunderseer.movementos.data.orchestration.RemoteUnavailableException
import com.theunderseer.movementos.data.orchestration.RepositoryOrchestration
import com.theunderseer.movementos.data.orchestration.networkBoundResource
import com.theunderseer.movementos.data.remote.SessionRemoteDataSource
import com.theunderseer.movementos.domain.common.DataError
import com.theunderseer.movementos.domain.common.DataState
import com.theunderseer.movementos.domain.model.ProgressEntry
import com.theunderseer.movementos.domain.model.Session
import com.theunderseer.movementos.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

private const val SYNC_TABLE = "Progress"

internal class DefaultSessionRepository(
    @Suppress("UnusedPrivateProperty")
    private val sessionDataSource: SessionLocalDataSource,
    private val progressDataSource: ProgressLocalDataSource,
    private val remote: SessionRemoteDataSource,
    private val orchestration: RepositoryOrchestration,
) : SessionRepository {
    override suspend fun recordCompletion(entry: ProgressEntry) =
        withContext(orchestration.dispatcher) {
            progressDataSource.upsert(entry)
        }

    override fun observeProgressHistory(forceRefresh: Boolean): Flow<DataState<List<ProgressEntry>>> =
        networkBoundResource(
            config =
                NetworkBoundResourceConfig(
                    loadFromLocal = { progressDataSource.observeAll() },
                    shouldFetch = { cached ->
                        forceRefresh ||
                            cached == null ||
                            orchestration.staleChecker.isStale(
                                orchestration.syncMetadata.getLastSyncedAt(SYNC_TABLE),
                            )
                    },
                    fetchRemote = {
                        orchestration.logger.d { "Fetching progress history from remote" }
                        remote.getProgressHistory()
                    },
                    saveRemoteResult = { fetched ->
                        fetched.forEach { progressDataSource.upsert(it) }
                        orchestration.syncMetadata.markSynced(SYNC_TABLE)
                        orchestration.logger.d { "Progress history refreshed and persisted" }
                    },
                    errorMapper = ::mapError,
                ),
            dispatcher = orchestration.dispatcher,
        )

    override suspend fun getProgressForSession(sessionId: String): List<ProgressEntry> =
        withContext(orchestration.dispatcher) {
            progressDataSource.getBySessionId(sessionId)
        }

    override suspend fun getNextSession(programSessions: List<Session>): Session? =
        withContext(orchestration.dispatcher) {
            if (programSessions.isEmpty()) return@withContext null
            val sorted = programSessions.sortedBy { it.orderIndex }
            val completionCounts = sorted.associateWith { progressDataSource.getBySessionId(it.id).size }
            val minCompletions = completionCounts.values.min()
            completionCounts.entries.first { it.value == minCompletions }.key
        }

    private fun mapError(throwable: Throwable): DataError {
        orchestration.logger.w(throwable) { "Remote fetch failed for progress history" }
        return when (throwable) {
            is RemoteNotFoundException -> DataError.NotFound
            is RemoteUnavailableException -> DataError.Network
            else -> DataError.Unknown(throwable.message)
        }
    }
}
