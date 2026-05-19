package com.theunderseer.movementos.domain.usecase

import com.theunderseer.movementos.domain.fake.FakeSessionRepository
import com.theunderseer.movementos.domain.model.ProgressEntry
import com.theunderseer.movementos.domain.model.UserGoal
import com.theunderseer.movementos.domain.model.values.DifficultyLevel
import com.theunderseer.movementos.domain.model.values.Duration
import com.theunderseer.movementos.domain.model.values.MovementType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class AdaptDifficultyUseCaseTest {
    private val repository = FakeSessionRepository()
    private val useCase = AdaptDifficultyUseCase(repository)

    private val baseGoal =
        UserGoal(
            id = "goal-1",
            description = "Stay mobile",
            focus = MovementType.MOBILITY,
            sessionsPerWeek = 3,
            timePerSession = Duration(1800),
            createdAt = Instant.fromEpochMilliseconds(1_700_000_000_000),
        )

    private fun entry(
        difficulty: DifficultyLevel,
        index: Int = 0,
    ) = ProgressEntry(
        id = "entry-$index",
        sessionId = "session-$index",
        completedAt = Instant.fromEpochMilliseconds(1_700_000_000_000),
        actualDuration = Duration(1800),
        perceivedDifficulty = difficulty,
    )

    @Test
    fun `returns original goal when history is empty`() =
        runTest {
            assertEquals(baseGoal, useCase(baseGoal))
        }

    @Test
    fun `returns original goal when fewer than 3 sessions recorded`() =
        runTest {
            repository.setHistory(listOf(entry(DifficultyLevel.TOO_EASY, 0), entry(DifficultyLevel.TOO_EASY, 1)))
            assertEquals(baseGoal, useCase(baseGoal))
        }

    @Test
    fun `increases timePerSession by 300s when last 3 sessions are TOO_EASY`() =
        runTest {
            repository.setHistory(
                listOf(
                    entry(DifficultyLevel.TOO_EASY, 0),
                    entry(DifficultyLevel.TOO_EASY, 1),
                    entry(DifficultyLevel.TOO_EASY, 2),
                ),
            )
            assertEquals(Duration(2100), useCase(baseGoal).timePerSession)
        }

    @Test
    fun `decreases timePerSession by 300s when last 3 sessions are TOO_HARD`() =
        runTest {
            repository.setHistory(
                listOf(
                    entry(DifficultyLevel.TOO_HARD, 0),
                    entry(DifficultyLevel.TOO_HARD, 1),
                    entry(DifficultyLevel.TOO_HARD, 2),
                ),
            )
            assertEquals(Duration(1500), useCase(baseGoal).timePerSession)
        }

    @Test
    fun `returns original goal when history is mixed`() =
        runTest {
            repository.setHistory(
                listOf(
                    entry(DifficultyLevel.TOO_EASY, 0),
                    entry(DifficultyLevel.JUST_RIGHT, 1),
                    entry(DifficultyLevel.TOO_HARD, 2),
                ),
            )
            assertEquals(baseGoal, useCase(baseGoal))
        }

    @Test
    fun `does not decrease timePerSession below 300s minimum`() =
        runTest {
            val shortGoal = baseGoal.copy(timePerSession = Duration(300))
            repository.setHistory(
                listOf(
                    entry(DifficultyLevel.TOO_HARD, 0),
                    entry(DifficultyLevel.TOO_HARD, 1),
                    entry(DifficultyLevel.TOO_HARD, 2),
                ),
            )
            assertEquals(Duration(300), useCase(shortGoal).timePerSession)
        }

    @Test
    fun `only considers the 3 most recent sessions`() =
        runTest {
            // First 3 entries are JUST_RIGHT (most recent); older ones are TOO_EASY — no change
            repository.setHistory(
                listOf(
                    entry(DifficultyLevel.JUST_RIGHT, 0),
                    entry(DifficultyLevel.JUST_RIGHT, 1),
                    entry(DifficultyLevel.JUST_RIGHT, 2),
                    entry(DifficultyLevel.TOO_EASY, 3),
                    entry(DifficultyLevel.TOO_EASY, 4),
                ),
            )
            assertEquals(baseGoal, useCase(baseGoal))
        }

    @Test
    fun `adapts difficulty from cached data in Loading state`() =
        runTest {
            repository.setLoading(
                cached =
                    listOf(
                        entry(DifficultyLevel.TOO_EASY, 0),
                        entry(DifficultyLevel.TOO_EASY, 1),
                        entry(DifficultyLevel.TOO_EASY, 2),
                    ),
            )
            assertEquals(Duration(2100), useCase(baseGoal).timePerSession)
        }

    @Test
    fun `returns original goal when Loading has no cached data`() =
        runTest {
            repository.setLoading(cached = null)
            assertEquals(baseGoal, useCase(baseGoal))
        }

    @Test
    fun `adapts difficulty from cached data in Error state`() =
        runTest {
            repository.setError(
                cached =
                    listOf(
                        entry(DifficultyLevel.TOO_HARD, 0),
                        entry(DifficultyLevel.TOO_HARD, 1),
                        entry(DifficultyLevel.TOO_HARD, 2),
                    ),
            )
            assertEquals(Duration(1500), useCase(baseGoal).timePerSession)
        }

    @Test
    fun `returns original goal when Error has no cached data`() =
        runTest {
            repository.setError(cached = null)
            assertEquals(baseGoal, useCase(baseGoal))
        }
}
