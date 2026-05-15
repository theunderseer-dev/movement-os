package com.theunderseer.movementos.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.theunderseer.movementos.data.local.GoalLocalDataSource
import com.theunderseer.movementos.database.MovementOSDatabase
import com.theunderseer.movementos.domain.model.UserGoal
import com.theunderseer.movementos.domain.model.values.Duration
import com.theunderseer.movementos.domain.model.values.MovementType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class DefaultGoalRepositoryTest {
    private lateinit var repository: DefaultGoalRepository
    private lateinit var database: MovementOSDatabase

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MovementOSDatabase.Schema.create(driver)
        database = MovementOSDatabase(driver)

        val goalDs = GoalLocalDataSource(database, Dispatchers.Unconfined)
        repository = DefaultGoalRepository(goalDs)
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
    fun `saves and retrieves goal by id`() =
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
    fun `observeCurrentGoal emits null initially then saved goal`() =
        runTest {
            repository.observeCurrentGoal().test {
                assertNull(awaitItem())

                repository.save(testGoal)
                assertEquals(testGoal.id, awaitItem()?.id)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `observeCurrentGoal emits most recently created goal`() =
        runTest {
            val older = testGoal.copy(id = "g-1", createdAt = Instant.fromEpochMilliseconds(1_700_000_000_000))
            val newer = testGoal.copy(id = "g-2", createdAt = Instant.fromEpochMilliseconds(1_800_000_000_000))

            repository.save(older)
            repository.save(newer)

            repository.observeCurrentGoal().test {
                assertEquals("g-2", awaitItem()?.id)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `saving goal with same id overwrites it`() =
        runTest {
            repository.save(testGoal)
            val updated = testGoal.copy(description = "Run a 5k")
            repository.save(updated)

            val retrieved = repository.getById(testGoal.id)
            assertEquals("Run a 5k", retrieved?.description)
        }
}
