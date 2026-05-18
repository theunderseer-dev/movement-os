package com.theunderseer.movementos.data.repository

import co.touchlab.kermit.Logger
import com.theunderseer.movementos.data.local.ProgressLocalDataSource
import com.theunderseer.movementos.data.local.SessionLocalDataSource
import com.theunderseer.movementos.data.local.SyncMetadataLocalDataSource
import com.theunderseer.movementos.data.orchestration.StaleChecker
import com.theunderseer.movementos.data.orchestration.networkBoundResource
import com.theunderseer.movementos.data.remote.SessionRemoteDataSource
import com.theunderseer.movementos.domain.common.DataError
import com.theunderseer.movementos.domain.common.DataState
import com.theunderseer.movementos.domain.model.ProgressEntry
import com.theunderseer.movementos.domain.model.Session
import com.theunderseer.movementos.domain.repository.SessionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

private const val SYNC_TABLE = "Progress"

internal class DefaultSessionRepository(
    @Suppress("UnusedPrivateProperty")
    private val sessionDataSource: SessionLocalDataSource,
    private val progressDataSource: ProgressLocalDataSource,
    private val remote: SessionRemoteDataSource,
    private val syncMetadata: SyncMetadataLocalDataSource,
    private val staleChecker: StaleChecker,
    private val dispatcher: CoroutineDispatcher,
    private val logger: Logger = Logger.withTag("SessionRepository"),
) : SessionRepository {
    override suspend fun recordCompletion(entry: ProgressEntry) =
        withContext(dispatcher) {
            progressDataSource.upsert(entry)
        }

    override fun observeProgressHistory(forceRefresh: Boolean): Flow<DataState<List<ProgressEntry>>> =
        networkBoundResource(
            loadFromLocal = { progressDataSource.observeAll() },
            shouldFetch = { cached ->
                forceRefresh ||
                    cached.isNullOrEmpty() ||
                    staleChecker.isStale(
                        syncMetadata.getLastSyncedAt(SYNC_TABLE),
                    )
            },
            fetchRemote = {
                logger.d { "Fetching progress history from remote" }
                remote.getProgressHistory()
            },
            saveRemoteResult = { entries ->
                entries.forEach { progressDataSource.upsert(it) }
                syncMetadata.markSynced(SYNC_TABLE)
            },
            errorMapper = ::mapError,
            dispatcher = dispatcher,
        )

    override suspend fun getProgressForSession(sessionId: String): List<ProgressEntry> =
        withContext(dispatcher) {
            progressDataSource.getBySessionId(sessionId)
        }

    override suspend fun getNextSession(programSessions: List<Session>): Session? =
        withContext(dispatcher) {
            if (programSessions.isEmpty()) return@withContext null
            val sorted = programSessions.sortedBy { it.orderIndex }
            val completionCounts = sorted.associateWith { progressDataSource.getBySessionId(it.id).size }
            val minCompletions = completionCounts.values.min()
            completionCounts.entries.first { it.value == minCompletions }.key
        }

    private fun mapError(throwable: Throwable): DataError {
        logger.w(throwable) { "Remote fetch failed for progress history" }
        return DataError.Unknown(throwable.message)
    }
}
