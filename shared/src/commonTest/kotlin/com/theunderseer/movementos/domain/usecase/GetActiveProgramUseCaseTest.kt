package com.theunderseer.movementos.domain.usecase

import app.cash.turbine.test
import com.theunderseer.movementos.core.testing.fixtures.TestPrograms
import com.theunderseer.movementos.domain.common.DataState
import com.theunderseer.movementos.domain.fake.FakeProgramRepository
import com.theunderseer.movementos.domain.model.Program
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class GetActiveProgramUseCaseTest {
    private val repository = FakeProgramRepository()
    private val useCase = GetActiveProgramUseCase(repository)

    private val testProgram = TestPrograms.aProgram(isActive = false)

    @Test
    fun `emits Error when no program is active`() =
        runTest {
            useCase().test {
                assertIs<DataState.Error<Program>>(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emits Success with saved program when one is generated`() =
        runTest {
            useCase().test {
                assertIs<DataState.Error<Program>>(awaitItem())

                repository.save(testProgram)

                val emitted = awaitItem()
                assertIs<DataState.Success<Program>>(emitted)
                assertEquals(testProgram.id, emitted.data.id)
                assertEquals(true, emitted.data.isActive)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emits new program when active program is replaced`() =
        runTest {
            val firstProgram = testProgram
            val secondProgram = TestPrograms.aProgram(id = "program-2", name = "Advanced plan")

            useCase().test {
                assertIs<DataState.Error<Program>>(awaitItem())

                repository.save(firstProgram)
                val first = awaitItem()
                assertIs<DataState.Success<Program>>(first)
                assertEquals("program-1", first.data.id)

                repository.save(secondProgram)
                val second = awaitItem()
                assertIs<DataState.Success<Program>>(second)
                assertEquals("program-2", second.data.id)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emits Error after program is deactivated`() =
        runTest {
            useCase().test {
                assertIs<DataState.Error<Program>>(awaitItem())

                repository.save(testProgram)
                val saved = awaitItem()
                assertIs<DataState.Success<Program>>(saved)

                repository.deactivate(testProgram.id)
                assertIs<DataState.Error<Program>>(awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }
}
