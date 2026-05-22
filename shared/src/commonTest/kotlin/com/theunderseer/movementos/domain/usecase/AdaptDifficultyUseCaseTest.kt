package com.theunderseer.movementos.domain.usecase

import com.theunderseer.movementos.core.testing.fixtures.TestGoals
import com.theunderseer.movementos.core.testing.fixtures.TestProgress
import com.theunderseer.movementos.domain.fake.FakeSessionRepository
import com.theunderseer.movementos.domain.model.values.DifficultyLevel
import com.theunderseer.movementos.domain.model.values.Duration
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class AdaptDifficultyUseCaseTest {
    private val repository = FakeSessionRepository()
    private val useCase = AdaptDifficultyUseCase(repository)

    private val baseGoal = TestGoals.aGoal(timePerSession = Duration.ofMinutes(20))

    @Test
    fun `returns goal unchanged when no progress history exists`() =
        runTest {
            val result = useCase(baseGoal)
            assertEquals(baseGoal, result)
        }

    @Test
    fun `returns goal unchanged when fewer than three sessions completed`() =
        runTest {
            repository.recordCompletion(
                TestProgress.aProgressEntry(
                    id = "e-1",
                    perceivedDifficulty = DifficultyLevel.TOO_EASY,
                ),
            )
            repository.recordCompletion(
                TestProgress.aProgressEntry(
                    id = "e-2",
                    perceivedDifficulty = DifficultyLevel.TOO_EASY,
                ),
            )

            val result = useCase(baseGoal)
            assertEquals(baseGoal, result)
        }

    @Test
    fun `increases time per session when last three sessions all too easy`() =
        runTest {
            repeat(3) { index ->
                repository.recordCompletion(
                    TestProgress.aProgressEntry(
                        id = "e-$index",
                        perceivedDifficulty = DifficultyLevel.TOO_EASY,
                    ),
                )
            }

            val result = useCase(baseGoal)

            assertEquals(Duration.ofMinutes(25), result.timePerSession)
            assertEquals(baseGoal.id, result.id)
            assertEquals(baseGoal.focus, result.focus)
        }

    @Test
    fun `decreases time per session when last three sessions all too hard`() =
        runTest {
            repeat(3) { index ->
                repository.recordCompletion(
                    TestProgress.aProgressEntry(
                        id = "e-$index",
                        perceivedDifficulty = DifficultyLevel.TOO_HARD,
                    ),
                )
            }

            val result = useCase(baseGoal)
            assertEquals(Duration.ofMinutes(15), result.timePerSession)
        }

    @Test
    fun `does not change goal when sessions are mixed`() =
        runTest {
            repository.recordCompletion(
                TestProgress.aProgressEntry(
                    id = "e-1",
                    perceivedDifficulty = DifficultyLevel.TOO_EASY,
                ),
            )
            repository.recordCompletion(
                TestProgress.aProgressEntry(
                    id = "e-2",
                    perceivedDifficulty = DifficultyLevel.JUST_RIGHT,
                ),
            )
            repository.recordCompletion(
                TestProgress.aProgressEntry(
                    id = "e-3",
                    perceivedDifficulty = DifficultyLevel.TOO_HARD,
                ),
            )

            val result = useCase(baseGoal)
            assertEquals(baseGoal, result)
        }

    @Test
    fun `does not reduce time below minimum threshold`() =
        runTest {
            val goalAtMinimum = baseGoal.copy(timePerSession = Duration.ofMinutes(5))
            repeat(3) { index ->
                repository.recordCompletion(
                    TestProgress.aProgressEntry(
                        id = "e-$index",
                        perceivedDifficulty = DifficultyLevel.TOO_HARD,
                    ),
                )
            }

            val result = useCase(goalAtMinimum)
            assertEquals(Duration.ofMinutes(5), result.timePerSession)
        }
}
