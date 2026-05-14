package com.theunderseer.movementos.domain.usecase

import com.theunderseer.movementos.domain.fake.FakeSessionRepository
import com.theunderseer.movementos.domain.model.Exercise
import com.theunderseer.movementos.domain.model.Session
import com.theunderseer.movementos.domain.model.values.DifficultyLevel
import com.theunderseer.movementos.domain.model.values.Duration
import com.theunderseer.movementos.domain.model.values.MovementType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class RecordSessionUseCaseTest {
    private val fixedInstant = Instant.fromEpochMilliseconds(1_700_000_000_000)
    private val fixedClock =
        object : Clock {
            override fun now(): Instant = fixedInstant
        }

    private val repository = FakeSessionRepository()
    private val useCase = RecordSessionUseCase(repository, fixedClock)

    private val testSession =
        Session(
            id = "session-1",
            programId = "program-1",
            orderIndex = 0,
            exercises =
                listOf(
                    Exercise(
                        id = "ex-1",
                        name = "Cat-cow",
                        duration = Duration.ofMinutes(2),
                        type = MovementType.MOBILITY,
                    ),
                ),
            estimatedDuration = Duration.ofMinutes(2),
        )

    @Test
    fun `records completed session with given difficulty`() =
        runTest {
            val entry =
                useCase(
                    session = testSession,
                    actualDuration = Duration.ofMinutes(3),
                    difficulty = DifficultyLevel.JUST_RIGHT,
                )

            assertEquals(testSession.id, entry.sessionId)
            assertEquals(DifficultyLevel.JUST_RIGHT, entry.perceivedDifficulty)
            assertEquals(Duration.ofMinutes(3), entry.actualDuration)
            assertEquals(fixedInstant, entry.completedAt)
        }

    @Test
    fun `generates deterministic id from session and timestamp`() =
        runTest {
            val entry = useCase(testSession, Duration.ofMinutes(2), DifficultyLevel.JUST_RIGHT)

            assertEquals("session-1-${fixedInstant.toEpochMilliseconds()}", entry.id)
        }

    @Test
    fun `treats blank notes as null`() =
        runTest {
            val entry =
                useCase(
                    session = testSession,
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
                    session = testSession,
                    actualDuration = Duration.ofMinutes(2),
                    difficulty = DifficultyLevel.JUST_RIGHT,
                    notes = "Felt tight in left hip",
                )

            assertEquals("Felt tight in left hip", entry.notes)
        }
}
