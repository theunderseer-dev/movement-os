package com.theunderseer.movementos.domain.usecase

import com.theunderseer.movementos.core.testing.fixtures.TestSessions
import com.theunderseer.movementos.core.testing.time.FixedClock
import com.theunderseer.movementos.domain.fake.FakeSessionRepository
import com.theunderseer.movementos.domain.model.values.DifficultyLevel
import com.theunderseer.movementos.domain.model.values.Duration
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class RecordSessionUseCaseTest {
    private val clock = FixedClock.default()
    private val repository = FakeSessionRepository()
    private val useCase = RecordSessionUseCase(repository, clock)

    @Test
    fun `records completed session with given difficulty`() =
        runTest {
            val session = TestSessions.aSession()

            val entry =
                useCase(
                    session = session,
                    actualDuration = Duration.ofMinutes(3),
                    difficulty = DifficultyLevel.JUST_RIGHT,
                )

            assertEquals(session.id, entry.sessionId)
            assertEquals(DifficultyLevel.JUST_RIGHT, entry.perceivedDifficulty)
            assertEquals(Duration.ofMinutes(3), entry.actualDuration)
            assertEquals(FixedClock.DEFAULT_INSTANT, entry.completedAt)
        }

    @Test
    fun `generates deterministic id from session and timestamp`() =
        runTest {
            val session = TestSessions.aSession(id = "session-42")

            val entry = useCase(session, Duration.ofMinutes(2), DifficultyLevel.JUST_RIGHT)

            assertEquals("session-42-${FixedClock.DEFAULT_INSTANT.toEpochMilliseconds()}", entry.id)
        }

    @Test
    fun `treats blank notes as null`() =
        runTest {
            val entry =
                useCase(
                    session = TestSessions.aSession(),
                    actualDuration = Duration.ofMinutes(2),
                    difficulty = DifficultyLevel.JUST_RIGHT,
                    notes = "   ",
                )

            assertNull(entry.notes)
        }

    @Test
    fun `preserves non-blank notes`() =
        runTest {
            val entry =
                useCase(
                    session = TestSessions.aSession(),
                    actualDuration = Duration.ofMinutes(2),
                    difficulty = DifficultyLevel.JUST_RIGHT,
                    notes = "Felt tight in left hip",
                )

            assertEquals("Felt tight in left hip", entry.notes)
        }
}
