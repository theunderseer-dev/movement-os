package com.theunderseer.movementos.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.theunderseer.movementos.data.local.ProgressLocalDataSource
import com.theunderseer.movementos.data.local.SessionLocalDataSource
import com.theunderseer.movementos.database.MovementOSDatabase
import com.theunderseer.movementos.domain.model.ProgressEntry
import com.theunderseer.movementos.domain.model.Session
import com.theunderseer.movementos.domain.model.values.DifficultyLevel
import com.theunderseer.movementos.domain.model.values.Duration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class DefaultSessionRepositoryTest {
    private lateinit var repository: DefaultSessionRepository
    private lateinit var database: MovementOSDatabase

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MovementOSDatabase.Schema.create(driver)
        database = MovementOSDatabase(driver)

        val sessionDs = SessionLocalDataSource(database, Dispatchers.Unconfined)
        val progressDs = ProgressLocalDataSource(database, Dispatchers.Unconfined)
        repository = DefaultSessionRepository(sessionDs, progressDs)
    }

    private val testEntry =
        ProgressEntry(
            id = "pe-1",
            sessionId = "s-1",
            completedAt = Instant.fromEpochMilliseconds(1_700_000_000_000),
            actualDuration = Duration.ofMinutes(30),
            perceivedDifficulty = DifficultyLevel.JUST_RIGHT,
        )

    private fun session(
        id: String,
        orderIndex: Int,
    ) = Session(
        id = id,
        programId = "p-1",
        orderIndex = orderIndex,
        exercises = emptyList(),
        estimatedDuration = Duration.ofMinutes(30),
    )

    // --- recordCompletion / getProgressForSession ---

    @Test
    fun `recordCompletion persists the entry`() =
        runTest {
            repository.recordCompletion(testEntry)

            val entries = repository.getProgressForSession("s-1")
            assertEquals(1, entries.size)
            assertEquals(testEntry.id, entries.first().id)
            assertEquals(testEntry.sessionId, entries.first().sessionId)
            assertEquals(testEntry.perceivedDifficulty, entries.first().perceivedDifficulty)
        }

    @Test
    fun `getProgressForSession returns empty list when no completions`() =
        runTest {
            assertTrue(repository.getProgressForSession("s-1").isEmpty())
        }

    @Test
    fun `getProgressForSession returns only entries for the given session`() =
        runTest {
            repository.recordCompletion(testEntry.copy(id = "pe-1", sessionId = "s-1"))
            repository.recordCompletion(testEntry.copy(id = "pe-2", sessionId = "s-2"))
            repository.recordCompletion(testEntry.copy(id = "pe-3", sessionId = "s-1"))

            val forSession1 = repository.getProgressForSession("s-1")
            assertEquals(2, forSession1.size)
            assertTrue(forSession1.all { it.sessionId == "s-1" })
        }

    @Test
    fun `recordCompletion allows duplicate entries for same session`() =
        runTest {
            repository.recordCompletion(testEntry.copy(id = "pe-1"))
            repository.recordCompletion(testEntry.copy(id = "pe-2"))

            assertEquals(2, repository.getProgressForSession("s-1").size)
        }

    // --- observeProgressHistory ---

    @Test
    fun `observeProgressHistory emits empty list initially`() =
        runTest {
            repository.observeProgressHistory().test {
                assertEquals(emptyList(), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `observeProgressHistory emits updated list after recordCompletion`() =
        runTest {
            repository.observeProgressHistory().test {
                assertEquals(emptyList(), awaitItem())

                repository.recordCompletion(testEntry)
                assertEquals(1, awaitItem().size)

                repository.recordCompletion(testEntry.copy(id = "pe-2"))
                assertEquals(2, awaitItem().size)

                cancelAndIgnoreRemainingEvents()
            }
        }

    // --- getNextSession ---

    @Test
    fun `getNextSession returns null for empty list`() =
        runTest {
            assertNull(repository.getNextSession(emptyList()))
        }

    @Test
    fun `getNextSession returns first session when none completed`() =
        runTest {
            val sessions = listOf(session("s-1", 0), session("s-2", 1), session("s-3", 2))

            assertEquals("s-1", repository.getNextSession(sessions)?.id)
        }

    @Test
    fun `getNextSession returns session with fewest completions`() =
        runTest {
            val sessions = listOf(session("s-1", 0), session("s-2", 1), session("s-3", 2))

            repository.recordCompletion(testEntry.copy(id = "pe-1", sessionId = "s-1"))
            repository.recordCompletion(testEntry.copy(id = "pe-2", sessionId = "s-1"))
            repository.recordCompletion(testEntry.copy(id = "pe-3", sessionId = "s-2"))
            // s-3 has 0 completions

            assertEquals("s-3", repository.getNextSession(sessions)?.id)
        }

    @Test
    fun `getNextSession returns first by orderIndex when tied`() =
        runTest {
            val sessions = listOf(session("s-1", 0), session("s-2", 1))

            repository.recordCompletion(testEntry.copy(id = "pe-1", sessionId = "s-1"))
            repository.recordCompletion(testEntry.copy(id = "pe-2", sessionId = "s-2"))
            // Both have 1 completion — s-1 has lower orderIndex

            assertEquals("s-1", repository.getNextSession(sessions)?.id)
        }

    @Test
    fun `getNextSession works with single session`() =
        runTest {
            val sessions = listOf(session("s-1", 0))

            assertEquals("s-1", repository.getNextSession(sessions)?.id)

            repository.recordCompletion(testEntry)
            assertEquals("s-1", repository.getNextSession(sessions)?.id)
        }
}
