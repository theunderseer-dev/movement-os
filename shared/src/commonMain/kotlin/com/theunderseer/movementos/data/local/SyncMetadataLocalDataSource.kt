package com.theunderseer.movementos.data.local

import com.theunderseer.movementos.database.MovementOSDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class SyncMetadataLocalDataSource(
    private val database: MovementOSDatabase,
    private val dispatcher: CoroutineDispatcher,
    private val clock: Clock = Clock.System,
) {
    private val queries get() = database.syncMetadataQueries

    suspend fun getLastSyncedAt(table: String): Long? =
        withContext(dispatcher) {
            queries.selectByTable(table).executeAsOneOrNull()?.last_synced_at_ms
        }

    suspend fun markSynced(table: String) =
        withContext(dispatcher) {
            queries.upsert(
                table_name = table,
                last_synced_at_ms = clock.now().toEpochMilliseconds(),
                last_sync_attempt_ms = clock.now().toEpochMilliseconds(),
                last_sync_error = null,
            )
        }
}
