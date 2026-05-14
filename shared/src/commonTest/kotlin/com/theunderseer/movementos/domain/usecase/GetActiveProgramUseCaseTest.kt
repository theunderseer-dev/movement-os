package com.theunderseer.movementos.domain.usecase

import app.cash.turbine.test
import com.theunderseer.movementos.domain.fake.FakeProgramRepository
import com.theunderseer.movementos.domain.model.Program
import com.theunderseer.movementos.domain.model.values.MovementType
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetActiveProgramUseCaseTest {
    private val repository = FakeProgramRepository()
    private val useCase = GetActiveProgramUseCase(repository)

    private val testProgram =
        Program(
            id = "program-1",
            goalId = "goal-1",
            name = "Back relief program",
            description = "10-day plan",
            primaryType = MovementType.BACK_PAIN_RELIEF,
            sessions = emptyList(),
            generatedAt = Instant.fromEpochMilliseconds(1_700_000_000_000),
            isActive = false,
        )

    @Test
    fun `emits null when no program is active`() =
        runTest {
            useCase().test {
                assertNull(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emits saved program when one is generated`() =
        runTest {
            useCase().test {
                assertNull(awaitItem())

                repository.save(testProgram)

                val emitted = awaitItem()
                assertEquals(testProgram.id, emitted?.id)
                assertEquals(true, emitted?.isActive)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emits new program when active program is replaced`() =
        runTest {
            val firstProgram = testProgram
            val secondProgram = testProgram.copy(id = "program-2", name = "Advanced plan")

            useCase().test {
                assertNull(awaitItem())

                repository.save(firstProgram)
                assertEquals("program-1", awaitItem()?.id)

                repository.save(secondProgram)
                assertEquals("program-2", awaitItem()?.id)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emits null after program is deactivated`() =
        runTest {
            useCase().test {
                assertNull(awaitItem())

                repository.save(testProgram)
                assertEquals(testProgram.id, awaitItem()?.id)

                repository.deactivate(testProgram.id)
                assertNull(awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }
}
