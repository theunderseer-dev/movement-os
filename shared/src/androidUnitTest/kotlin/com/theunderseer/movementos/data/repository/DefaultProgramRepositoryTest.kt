package com.theunderseer.movementos.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import co.touchlab.kermit.Logger
import com.theunderseer.movementos.core.testing.fixtures.TestPrograms
import com.theunderseer.movementos.data.local.ProgramLocalDataSource
import com.theunderseer.movementos.data.local.SessionLocalDataSource
import com.theunderseer.movementos.data.local.SyncMetadataLocalDataSource
import com.theunderseer.movementos.data.orchestration.RepositoryOrchestration
import com.theunderseer.movementos.data.orchestration.StaleChecker
import com.theunderseer.movementos.data.remote.ProgramRemoteDataSource
import com.theunderseer.movementos.database.MovementOSDatabase
import com.theunderseer.movementos.domain.common.DataError
import com.theunderseer.movementos.domain.common.DataState
import com.theunderseer.movementos.domain.model.Program
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class DefaultProgramRepositoryTest {
    private lateinit var repository: DefaultProgramRepository
    private lateinit var local: ProgramLocalDataSource
    private val remote = FakeProgramRemoteDataSource()
    private lateinit var syncMetadata: SyncMetadataLocalDataSource

    private lateinit var orchestration: RepositoryOrchestration

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MovementOSDatabase.Schema.create(driver)
        val db = MovementOSDatabase(driver)
        val sessionDs = SessionLocalDataSource(db, Dispatchers.Unconfined)
        local = ProgramLocalDataSource(db, sessionDs, Dispatchers.Unconfined)
        syncMetadata = SyncMetadataLocalDataSource(db, Dispatchers.Unconfined)
        orchestration =
            RepositoryOrchestration(
                syncMetadata,
                StaleChecker(ttl = 1.hours),
                Dispatchers.Unconfined,
                Logger.withTag("test"),
            )
        repository =
            DefaultProgramRepository(
                local = local,
                remote = remote,
                orchestration = orchestration,
            )
    }

    private val testProgram = TestPrograms.aProgram()

    @Test
    fun `cache hit - emits Loading then Success from local without remote call`() =
        runTest {
            local.save(testProgram)
            syncMetadata.markSynced("Programs")

            repository.observeActiveProgram().test {
                val loading = awaitItem()
                assertIs<DataState.Loading<Program>>(loading)
                assertEquals(testProgram.id, loading.cached?.id)

                val success = awaitItem()
                assertIs<DataState.Success<Program>>(success)
                assertEquals(testProgram.id, success.data.id)
                assertEquals(0, remote.fetchCount)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `cache miss - fetches remote, saves to local, emits Success`() =
        runTest {
            remote.programToReturn = testProgram

            repository.observeActiveProgram().test {
                val loading = awaitItem()
                assertIs<DataState.Loading<Program>>(loading)
                assertNull(loading.cached)

                val success = awaitItem()
                assertIs<DataState.Success<Program>>(success)
                assertEquals(testProgram.id, success.data.id)
                assertEquals(1, remote.fetchCount)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `network failure with cached data - emits Error with cached fallback`() =
        runTest {
            local.save(testProgram)
            remote.shouldThrow = true

            repository.observeActiveProgram().test {
                val loading = awaitItem()
                assertIs<DataState.Loading<Program>>(loading)

                val error = awaitItem()
                assertIs<DataState.Error<Program>>(error)
                assertEquals(testProgram.id, error.cached?.id)
                assertEquals(DataError.Unknown::class, error.error::class)

                val success = awaitItem()
                assertIs<DataState.Success<Program>>(success)
                assertEquals(testProgram.id, success.data.id)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `forceRefresh - triggers fetch even when fresh`() =
        runTest {
            local.save(testProgram)
            syncMetadata.markSynced("Programs")
            remote.programToReturn = TestPrograms.aProgram(name = "Refreshed plan")

            repository.observeActiveProgram(forceRefresh = true).test {
                awaitItem()

                val success = awaitItem()
                assertIs<DataState.Success<Program>>(success)
                assertEquals(1, remote.fetchCount)

                cancelAndIgnoreRemainingEvents()
            }
        }
}

private class FakeProgramRemoteDataSource : ProgramRemoteDataSource {
    var programToReturn: Program? = null
    var shouldThrow: Boolean = false
    var fetchCount: Int = 0

    override suspend fun getActive(): Program? {
        fetchCount++
        if (shouldThrow) throw RuntimeException("Network down")
        return programToReturn
    }

    override suspend fun upsert(program: Program) = Unit
}
