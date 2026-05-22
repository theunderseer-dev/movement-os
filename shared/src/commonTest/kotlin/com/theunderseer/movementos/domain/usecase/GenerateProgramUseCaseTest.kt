package com.theunderseer.movementos.domain.usecase

import com.theunderseer.movementos.core.testing.fixtures.TestGoals
import com.theunderseer.movementos.core.testing.fixtures.TestPrograms
import com.theunderseer.movementos.core.testing.time.FixedClock
import com.theunderseer.movementos.domain.fake.FakeProgramRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class GenerateProgramUseCaseTest {
    private val testGoal = TestGoals.aGoal()

    @Test
    fun `generates program and persists as active`() =
        runTest {
            val repo = FakeProgramRepository()
            val generator =
                ProgramGenerator { goal ->
                    TestPrograms.aProgram(
                        id = "program-1",
                        goalId = goal.id,
                        generatedAt = FixedClock.DEFAULT_INSTANT,
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
