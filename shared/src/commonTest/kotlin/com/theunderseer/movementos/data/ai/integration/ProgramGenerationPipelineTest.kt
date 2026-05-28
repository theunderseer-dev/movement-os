package com.theunderseer.movementos.data.ai.integration

import com.theunderseer.movementos.core.testing.ai.FakeLlmClient
import com.theunderseer.movementos.core.testing.ai.fixtures.ProgramGenerationFixtures
import com.theunderseer.movementos.core.testing.fixtures.TestGoals
import com.theunderseer.movementos.core.testing.time.FixedClock
import com.theunderseer.movementos.data.ai.prompt.program.ProgramGenerator
import com.theunderseer.movementos.domain.model.values.MovementType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class ProgramGenerationPipelineTest {
    private val goal = TestGoals.aGoal(focus = MovementType.MOBILITY)
    private val clock = FixedClock.default()

    @Test
    fun `generates program from valid LLM JSON response`() =
        runTest {
            val llmClient = FakeLlmClient().respondWithSuccess(ProgramGenerationFixtures.validProgramJson)
            val generator = ProgramGenerator(llmClient = llmClient, clock = clock)

            val program = generator.generate(goal)

            assertEquals("10-Day Mobility Program", program.name)
            assertEquals(MovementType.MOBILITY, program.primaryType)
            assertEquals(1, program.sessions.size)
            assertEquals(2, program.sessions[0].exercises.size)
        }

    @Test
    fun `parses markdown-wrapped JSON response`() =
        runTest {
            val llmClient =
                FakeLlmClient().respondWithSuccess(
                    ProgramGenerationFixtures.validProgramJsonWithMarkdown,
                )
            val generator = ProgramGenerator(llmClient = llmClient, clock = clock)

            val program = generator.generate(goal)

            assertEquals("10-Day Mobility Program", program.name)
        }

    @Test
    fun `parses prose-prefixed JSON response`() =
        runTest {
            val llmClient =
                FakeLlmClient().respondWithSuccess(
                    ProgramGenerationFixtures.validProgramJsonWithProse,
                )
            val generator = ProgramGenerator(llmClient = llmClient, clock = clock)

            val program = generator.generate(goal)

            assertEquals("10-Day Mobility Program", program.name)
        }

    @Test
    fun `throws on malformed JSON`() =
        runTest {
            val llmClient = FakeLlmClient().respondWithSuccess(ProgramGenerationFixtures.MALFORMED_JSON)
            val generator = ProgramGenerator(llmClient = llmClient, clock = clock)

            assertFailsWith<IllegalStateException> { generator.generate(goal) }
        }

    @Test
    fun `throws on missing required field`() =
        runTest {
            val llmClient = FakeLlmClient().respondWithSuccess(ProgramGenerationFixtures.missingFieldJson)
            val generator = ProgramGenerator(llmClient = llmClient, clock = clock)

            assertFailsWith<IllegalStateException> { generator.generate(goal) }
        }

    @Test
    fun `throws on invalid MovementType`() =
        runTest {
            val llmClient = FakeLlmClient().respondWithSuccess(ProgramGenerationFixtures.invalidMovementTypeJson)
            val generator = ProgramGenerator(llmClient = llmClient, clock = clock)

            assertFailsWith<IllegalStateException> { generator.generate(goal) }
        }

    @Test
    fun `generates deterministic IDs from goal and timestamp`() =
        runTest {
            val llmClient = FakeLlmClient().respondWithSuccess(ProgramGenerationFixtures.validProgramJson)
            val generator = ProgramGenerator(llmClient = llmClient, clock = clock)

            val program = generator.generate(goal)
            val expectedPrefix = "program-${goal.id}-${FixedClock.DEFAULT_INSTANT.toEpochMilliseconds()}"

            assertTrue(program.id.startsWith(expectedPrefix))
            assertTrue(program.sessions[0].id.startsWith(expectedPrefix))
            assertTrue(
                program.sessions[0]
                    .exercises[0]
                    .id
                    .startsWith(expectedPrefix),
            )
        }
}
