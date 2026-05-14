package com.theunderseer.movementos.domain.usecase

import com.theunderseer.movementos.domain.fake.FakeProgramRepository
import com.theunderseer.movementos.domain.model.Program
import com.theunderseer.movementos.domain.model.UserGoal
import com.theunderseer.movementos.domain.model.values.Duration
import com.theunderseer.movementos.domain.model.values.MovementType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class GenerateProgramUseCaseTest {
    private val testGoal =
        UserGoal(
            id = "goal-1",
            description = "Fix back pain",
            focus = MovementType.BACK_PAIN_RELIEF,
            sessionsPerWeek = 3,
            timePerSession = Duration.ofMinutes(15),
            createdAt = Instant.fromEpochMilliseconds(1_700_000_000_000),
        )

    @Test
    fun `generates program and persists as active`() =
        runTest {
            val repo = FakeProgramRepository()
            val generator =
                ProgramGenerator { goal ->
                    Program(
                        id = "program-1",
                        goalId = goal.id,
                        name = "Back relief program",
                        description = "10-day plan",
                        primaryType = goal.focus,
                        sessions = emptyList(),
                        generatedAt = Clock.System.now(),
                        isActive = false,
                    )
                }
            val useCase = GenerateProgramUseCase(repo, generator)

            val result = useCase(testGoal)

            assertTrue(result.isSuccess)
            val saved = repo.getById("program-1")
            assertNotNull(saved)
            assertTrue(saved.isActive)
        }

    @Test
    fun `returns failure when generator throws`() =
        runTest {
            val repo = FakeProgramRepository()
            val generator = ProgramGenerator { error("LLM unavailable") }
            val useCase = GenerateProgramUseCase(repo, generator)

            val result = useCase(testGoal)

            assertTrue(result.isFailure)
            assertEquals("LLM unavailable", result.exceptionOrNull()?.message)
        }
}
