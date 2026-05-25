package com.theunderseer.movementos.data.ai.prompt.program

import com.theunderseer.movementos.domain.model.Exercise
import com.theunderseer.movementos.domain.model.Program
import com.theunderseer.movementos.domain.model.Session
import com.theunderseer.movementos.domain.model.UserGoal
import com.theunderseer.movementos.domain.model.values.Duration
import com.theunderseer.movementos.domain.model.values.MovementType
import com.theunderseer.movementos.domain.usecase.ProgramGenerator
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Rule-based fallback program generator.
 *
 * Used when LLM is unavailable or returns invalid responses.
 * Output is intentionally generic, sufficient to start practicing,
 * users get personalized programs once LLM recovers.
 */
@OptIn(ExperimentalTime::class)
internal class DeterministicProgramGenerator(
    private val clock: Clock = Clock.System,
) : ProgramGenerator {
    override suspend fun generate(goal: UserGoal): Program {
        val now = clock.now()
        val programId = "program-fallback-${goal.id}-${now.toEpochMilliseconds()}"

        val templateExercises = exercisesFor(goal.focus)
        val sessions =
            (0 until goal.sessionsPerWeek).map { sessionIndex ->
                Session(
                    id = "$programId-session-$sessionIndex",
                    programId = programId,
                    orderIndex = sessionIndex,
                    exercises =
                        templateExercises.mapIndexed { exerciseIndex, template ->
                            Exercise(
                                id = "$programId-session-$sessionIndex-exercise-$exerciseIndex",
                                name = template.first,
                                duration = Duration(template.second),
                                type = goal.focus,
                                cues = template.third,
                            )
                        },
                    estimatedDuration = goal.timePerSession,
                )
            }

        return Program(
            id = programId,
            goalId = goal.id,
            name = "Basic ${goal.focus.name.lowercase().replace('_', ' ')} program",
            description = "A foundational program to start practicing. Will be personalized once AI is available.",
            primaryType = goal.focus,
            sessions = sessions,
            generatedAt = now,
            isActive = true,
        )
    }

    private fun exercisesFor(focus: MovementType): List<Triple<String, Int, List<String>>> =
        when (focus) {
            MovementType.MOBILITY -> {
                listOf(
                    Triple("Cat-cow", 120, listOf("Inhale arching back", "Exhale rounding spine")),
                    Triple("Hip circles", 90, listOf("Slow controlled circles", "Both directions")),
                    Triple("Shoulder rolls", 60, listOf("Roll back and down", "Open chest")),
                )
            }

            MovementType.STRENGTH -> {
                listOf(
                    Triple("Bodyweight squat", 180, listOf("Keep heels grounded", "Knees tracking over toes")),
                    Triple("Plank", 60, listOf("Engage core", "Flat back")),
                    Triple("Glute bridge", 120, listOf("Squeeze glutes at top", "Slow controlled descent")),
                )
            }

            MovementType.FLEXIBILITY -> {
                listOf(
                    Triple("Forward fold", 120, listOf("Bend from hips", "Soft knees if needed")),
                    Triple("Pigeon pose", 180, listOf("Square hips", "Breathe into the stretch")),
                    Triple("Seated twist", 90, listOf("Lengthen spine before twisting", "Both sides")),
                )
            }

            MovementType.BALANCE -> {
                listOf(
                    Triple("Single leg stand", 60, listOf("Engage core", "Soft knee")),
                    Triple("Tree pose", 90, listOf("Foot on thigh or calf", "Drishti (focused gaze)")),
                    Triple("Tandem stance", 60, listOf("Heel to toe", "Steady breath")),
                )
            }

            MovementType.RECOVERY -> {
                listOf(
                    Triple("Child's pose", 180, listOf("Knees wide", "Forehead to floor")),
                    Triple("Legs up the wall", 300, listOf("Relax fully", "Breathe deeply")),
                    Triple("Reclined twist", 120, listOf("Knees to chest", "Drop to one side")),
                )
            }

            MovementType.BACK_PAIN_RELIEF -> {
                listOf(
                    Triple("Knees to chest", 120, listOf("Slow controlled motion", "Breathe deeply")),
                    Triple("Cat-cow", 120, listOf("Inhale arching", "Exhale rounding")),
                    Triple("Sphinx pose", 180, listOf("Press forearms down", "Lift chest gently")),
                )
            }
        }
}
