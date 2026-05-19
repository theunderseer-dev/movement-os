package com.theunderseer.movementos.data.orchestration

import co.touchlab.kermit.Logger
import com.theunderseer.movementos.data.local.SyncMetadataLocalDataSource
import kotlinx.coroutines.CoroutineDispatcher

internal data class RepositoryOrchestration(
    val syncMetadata: SyncMetadataLocalDataSource,
    val staleChecker: StaleChecker,
    val dispatcher: CoroutineDispatcher,
    val logger: Logger = Logger.withTag("Repository"),
)
