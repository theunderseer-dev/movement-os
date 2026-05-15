package com.theunderseer.movementos.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.theunderseer.movementos.data.local.ProgramLocalDataSource
import com.theunderseer.movementos.data.local.SessionLocalDataSource
import com.theunderseer.movementos.database.MovementOSDatabase
import com.theunderseer.movementos.domain.model.Program
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
class DefaultProgramRepositoryTest {
    private lateinit var repository: DefaultProgramRepository
    private lateinit var database: MovementOSDatabase

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MovementOSDatabase.Schema.create(driver)
        database = MovementOSDatabase(driver)

        val sessionDs = SessionLocalDataSource(database, Dispatchers.Unconfined)
        val programDs = ProgramLocalDataSource(database, sessionDs, Dispatchers.Unconfined)
        repository = DefaultProgramRepository(programDs)
    }

    private val testProgram =
        Program(
            id = "p-1",
            goalId = "g-1",
            name = "Back relief",
            description = "10-day plan",
            primaryType = MovementType.BACK_PAIN_RELIEF,
            sessions = emptyList(),
            generatedAt = Instant.fromEpochMilliseconds(1_700_000_000_000),
            isActive = true,
        )

    @Test
    fun `saves and retrieves program by id`() =
        runTest {
            repository.save(testProgram)

            val retrieved = repository.getById(testProgram.id)
            assertEquals(testProgram.id, retrieved?.id)
            assertEquals(testProgram.name, retrieved?.name)
        }

    @Test
    fun `observeActiveProgram emits null initially then saved program`() =
        runTest {
            repository.observeActiveProgram().test {
                assertNull(awaitItem())

                repository.save(testProgram)
                assertEquals(testProgram.id, awaitItem()?.id)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `saving second program deactivates the first`() =
        runTest {
            repository.save(testProgram)
            val second = testProgram.copy(id = "p-2", name = "Advanced")
            repository.save(second)

            val first = repository.getById("p-1")
            val secondRetrieved = repository.getById("p-2")
            assertEquals(false, first?.isActive)
            assertEquals(true, secondRetrieved?.isActive)
        }

    @Test
    fun `deactivate marks program inactive without deleting`() =
        runTest {
            repository.save(testProgram)
            repository.deactivate(testProgram.id)

            val retrieved = repository.getById(testProgram.id)
            assertEquals(false, retrieved?.isActive)
        }
}
