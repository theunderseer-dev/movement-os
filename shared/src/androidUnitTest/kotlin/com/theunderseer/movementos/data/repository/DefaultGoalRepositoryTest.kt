package com.theunderseer.movementos.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import co.touchlab.kermit.Logger
import com.theunderseer.movementos.data.local.GoalLocalDataSource
import com.theunderseer.movementos.data.local.SyncMetadataLocalDataSource
import com.theunderseer.movementos.data.orchestration.RepositoryOrchestration
import com.theunderseer.movementos.data.orchestration.StaleChecker
import com.theunderseer.movementos.data.remote.GoalRemoteDataSource
import com.theunderseer.movementos.database.MovementOSDatabase
import com.theunderseer.movementos.domain.common.DataError
import com.theunderseer.movementos.domain.common.DataState
import com.theunderseer.movementos.domain.model.UserGoal
import com.theunderseer.movementos.domain.model.values.Duration
import com.theunderseer.movementos.domain.model.values.MovementType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class DefaultGoalRepositoryTest {
    private lateinit var repository: DefaultGoalRepository
    private lateinit var local: GoalLocalDataSource
    private val remote = FakeGoalRemoteDataSource()
    private lateinit var syncMetadata: SyncMetadataLocalDataSource

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MovementOSDatabase.Schema.create(driver)
        val db = MovementOSDatabase(driver)
        local = GoalLocalDataSource(db, Dispatchers.Unconfined)
        syncMetadata = SyncMetadataLocalDataSource(db, Dispatchers.Unconfined)
        repository =
            DefaultGoalRepository(
                local = local,
                remote = remote,
                orchestration =
                    RepositoryOrchestration(
                        syncMetadata = syncMetadata,
                        staleChecker = StaleChecker(ttl = 1.hours),
                        dispatcher = Dispatchers.Unconfined,
                        logger = Logger.withTag("test"),
                    ),
            )
    }

    private val testGoal =
        UserGoal(
            id = "g-1",
            description = "Touch my toes by August",
            focus = MovementType.FLEXIBILITY,
            sessionsPerWeek = 3,
            timePerSession = Duration.ofMinutes(20),
            createdAt = Instant.fromEpochMilliseconds(1_700_000_000_000),
        )

    @Test
    fun `cache hit - emits Loading then Success from local without remote call`() =
        runTest {
            local.save(testGoal)
            syncMetadata.markSynced("Goals")

            repository.observeCurrentGoal().test {
                val loading = awaitItem()
                assertIs<DataState.Loading<UserGoal>>(loading)
                assertEquals(testGoal.id, loading.cached?.id)

                val success = awaitItem()
                assertIs<DataState.Success<UserGoal>>(success)
                assertEquals(testGoal.id, success.data.id)
                assertEquals(0, remote.fetchCount)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `cache miss - fetches remote, saves to local, emits Success`() =
        runTest {
            remote.goalToReturn = testGoal

            repository.observeCurrentGoal().test {
                val loading = awaitItem()
                assertIs<DataState.Loading<UserGoal>>(loading)
                assertNull(loading.cached)

                val success = awaitItem()
                assertIs<DataState.Success<UserGoal>>(success)
                assertEquals(testGoal.id, success.data.id)
                assertEquals(1, remote.fetchCount)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `network failure with cached data - emits Error with cached fallback`() =
        runTest {
            local.save(testGoal)
            // don't markSynced -> stale -> triggers fetch
            remote.shouldThrow = true

            repository.observeCurrentGoal().test {
                val loading = awaitItem()
                assertIs<DataState.Loading<UserGoal>>(loading)

                val error = awaitItem()
                assertIs<DataState.Error<UserGoal>>(error)
                assertEquals(testGoal.id, error.cached?.id)
                assertEquals(DataError.Unknown::class, error.error::class)

                // local Flow still emits cached
                val success = awaitItem()
                assertIs<DataState.Success<UserGoal>>(success)
                assertEquals(testGoal.id, success.data.id)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `forceRefresh - triggers fetch even when fresh`() =
        runTest {
            local.save(testGoal)
            syncMetadata.markSynced("Goals")
            remote.goalToReturn = testGoal.copy(description = "Updated goal")

            repository.observeCurrentGoal(forceRefresh = true).test {
                awaitItem() // Loading

                val success = awaitItem()
                assertIs<DataState.Success<UserGoal>>(success)
                assertEquals(1, remote.fetchCount)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `getById returns saved goal`() =
        runTest {
            repository.save(testGoal)

            val retrieved = repository.getById(testGoal.id)
            assertEquals(testGoal.id, retrieved?.id)
            assertEquals(testGoal.description, retrieved?.description)
            assertEquals(testGoal.focus, retrieved?.focus)
            assertEquals(testGoal.sessionsPerWeek, retrieved?.sessionsPerWeek)
            assertEquals(testGoal.timePerSession, retrieved?.timePerSession)
        }

    @Test
    fun `getById returns null for unknown id`() =
        runTest {
            assertNull(repository.getById("unknown"))
        }

    @Test
    fun `save overwrites existing goal with same id`() =
        runTest {
            repository.save(testGoal)
            repository.save(testGoal.copy(description = "Run a 5k"))

            val retrieved = repository.getById(testGoal.id)
            assertEquals("Run a 5k", retrieved?.description)
        }
}

private class FakeGoalRemoteDataSource : GoalRemoteDataSource {
    var goalToReturn: UserGoal? = null
    var shouldThrow: Boolean = false
    var fetchCount: Int = 0

    override suspend fun getCurrent(): UserGoal? {
        fetchCount++
        if (shouldThrow) throw RuntimeException("Network down")
        return goalToReturn
    }

    override suspend fun getById(id: String): UserGoal? = goalToReturn?.takeIf { it.id == id }

    override suspend fun upsert(goal: UserGoal) = Unit
}
