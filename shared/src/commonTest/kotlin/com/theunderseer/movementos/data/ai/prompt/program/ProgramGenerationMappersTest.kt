package com.theunderseer.movementos.data.ai.prompt.program

import com.theunderseer.movementos.core.testing.fixtures.TestGoals
import com.theunderseer.movementos.domain.model.values.MovementType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class ProgramGenerationMappersTest {
    private val goal = TestGoals.aGoal(focus = MovementType.MOBILITY)
    private val now = Instant.Companion.fromEpochMilliseconds(1_700_000_000_000)

    @Test
    fun `maps valid response to domain Program`() {
        val response =
            ProgramGenerationResponse(
                name = "Mobility plan",
                description = "10-day mobility",
                primaryType = "MOBILITY",
                sessions =
                    listOf(
                        ProgramSessionResponse(
                            orderIndex = 0,
                            estimatedDurationMinutes = 20,
                            exercises =
                                listOf(
                                    ProgramExerciseResponse(
                                        name = "Cat-cow",
                                        durationSeconds = 120,
                                        type = "MOBILITY",
                                        cues = listOf("Inhale", "Exhale"),
                                    ),
                                ),
                        ),
                    ),
            )

        val result = response.toDomain(goal, now)

        assertTrue(result.isSuccess)
        val program = result.getOrNull()!!
        assertEquals("Mobility plan", program.name)
        assertEquals(MovementType.MOBILITY, program.primaryType)
        assertEquals(1, program.sessions.size)
        assertEquals("Cat-cow", program.sessions[0].exercises[0].name)
    }

    @Test
    fun `fails on unknown MovementType`() {
        val response =
            ProgramGenerationResponse(
                name = "Plan",
                description = "Test",
                primaryType = "UNKNOWN_TYPE",
                sessions = emptyList(),
            )

        val result = response.toDomain(goal, now)

        assertTrue(result.isFailure)
    }

    @Test
    fun `generates deterministic ids based on goal and timestamp`() {
        val response =
            ProgramGenerationResponse(
                name = "Plan",
                description = "Test",
                primaryType = "MOBILITY",
                sessions =
                    listOf(
                        ProgramSessionResponse(
                            orderIndex = 0,
                            estimatedDurationMinutes = 20,
                            exercises = emptyList(),
                        ),
                    ),
            )

        val result = response.toDomain(goal, now).getOrThrow()

        assertEquals("program-${goal.id}-${now.toEpochMilliseconds()}", result.id)
        assertEquals("${result.id}-session-0", result.sessions[0].id)
    }
}
