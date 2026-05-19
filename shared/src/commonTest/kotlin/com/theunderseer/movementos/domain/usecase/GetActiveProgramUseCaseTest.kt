package com.theunderseer.movementos.domain.usecase

import app.cash.turbine.test
import com.theunderseer.movementos.domain.common.DataState
import com.theunderseer.movementos.domain.fake.FakeProgramRepository
import com.theunderseer.movementos.domain.model.Program
import com.theunderseer.movementos.domain.model.values.MovementType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
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
    fun `emits error when no program is active`() =
        runTest {
            useCase().test {
                assertIs<DataState.Error<Program>>(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emits saved program when one is generated`() =
        runTest {
            useCase().test {
                assertIs<DataState.Error<Program>>(awaitItem())

                repository.save(testProgram)

                val emitted = assertIs<DataState.Success<Program>>(awaitItem())
                assertEquals(testProgram.id, emitted.data.id)
                assertEquals(true, emitted.data.isActive)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emits new program when active program is replaced`() =
        runTest {
            val firstProgram = testProgram
            val secondProgram = testProgram.copy(id = "program-2", name = "Advanced plan")

            useCase().test {
                assertIs<DataState.Error<Program>>(awaitItem())

                repository.save(firstProgram)
                assertEquals("program-1", assertIs<DataState.Success<Program>>(awaitItem()).data.id)

                repository.save(secondProgram)
                assertEquals("program-2", assertIs<DataState.Success<Program>>(awaitItem()).data.id)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emits error after program is deactivated`() =
        runTest {
            useCase().test {
                assertIs<DataState.Error<Program>>(awaitItem())

                repository.save(testProgram)
                assertEquals(testProgram.id, assertIs<DataState.Success<Program>>(awaitItem()).data.id)

                repository.deactivate(testProgram.id)
                assertIs<DataState.Error<Program>>(awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }
}
