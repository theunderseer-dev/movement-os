package com.theunderseer.movementos.domain.usecase

import com.theunderseer.movementos.core.testing.fixtures.TestGoals
import com.theunderseer.movementos.core.testing.fixtures.TestPrograms
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
    fun `generates program via primary generator and persists as active`() =
        runTest {
            val repo = FakeProgramRepository()
            val primary =
                ProgramGenerator { goal ->
                    TestPrograms.aProgram(id = "program-1", goalId = goal.id, isActive = false)
                }
            val fallback = ProgramGenerator { error("should not be called") }
            val useCase = GenerateProgramUseCase(repo, primary, fallback)

            val result = useCase(testGoal)

            assertTrue(result.isSuccess)
            val saved = repo.getById("program-1")
            assertNotNull(saved)
            assertTrue(saved.isActive)
        }

    @Test
    fun `falls back to fallback generator when primary throws`() =
        runTest {
            val repo = FakeProgramRepository()
            val primary = ProgramGenerator { error("LLM unavailable") }
            val fallback =
                ProgramGenerator { goal ->
                    TestPrograms.aProgram(id = "program-fallback", goalId = goal.id, isActive = false)
                }
            val useCase = GenerateProgramUseCase(repo, primary, fallback)

            val result = useCase(testGoal)

            assertTrue(result.isSuccess)
            assertNotNull(repo.getById("program-fallback"))
        }

    @Test
    fun `returns failure when both primary and fallback throw`() =
        runTest {
            val repo = FakeProgramRepository()
            val primary = ProgramGenerator { error("LLM unavailable") }
            val fallback = ProgramGenerator { error("Fallback unavailable") }
            val useCase = GenerateProgramUseCase(repo, primary, fallback)

            val result = useCase(testGoal)

            assertTrue(result.isFailure)
            assertEquals("Fallback unavailable", result.exceptionOrNull()?.message)
        }
}
