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
                                name = template.name,
                                duration = Duration(template.durationSeconds),
                                type = goal.focus,
                                cues = template.cues,
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

    private fun exercisesFor(focus: MovementType): List<ExerciseTemplate> = EXERCISES_BY_FOCUS[focus] ?: emptyList()

    private data class ExerciseTemplate(
        val name: String,
        val durationSeconds: Int,
        val cues: List<String>,
    )

    private companion object {
        const val QUICK = 60
        const val BRIEF = 90
        const val SHORT = 120
        const val MEDIUM = 180
        const val LONG = 300

        val EXERCISES_BY_FOCUS: Map<MovementType, List<ExerciseTemplate>> =
            mapOf(
                MovementType.MOBILITY to
                    listOf(
                        ExerciseTemplate(
                            "Cat-cow",
                            SHORT,
                            listOf("Inhale arching back", "Exhale rounding spine"),
                        ),
                        ExerciseTemplate(
                            "Hip circles",
                            BRIEF,
                            listOf("Slow controlled circles", "Both directions"),
                        ),
                        ExerciseTemplate(
                            "Shoulder rolls",
                            QUICK,
                            listOf("Roll back and down", "Open chest"),
                        ),
                    ),
                MovementType.STRENGTH to
                    listOf(
                        ExerciseTemplate(
                            "Bodyweight squat",
                            MEDIUM,
                            listOf("Keep heels grounded", "Knees tracking over toes"),
                        ),
                        ExerciseTemplate(
                            "Plank",
                            QUICK,
                            listOf("Engage core", "Flat back"),
                        ),
                        ExerciseTemplate(
                            "Glute bridge",
                            SHORT,
                            listOf("Squeeze glutes at top", "Slow controlled descent"),
                        ),
                    ),
                MovementType.FLEXIBILITY to
                    listOf(
                        ExerciseTemplate(
                            "Forward fold",
                            SHORT,
                            listOf("Bend from hips", "Soft knees if needed"),
                        ),
                        ExerciseTemplate(
                            "Pigeon pose",
                            MEDIUM,
                            listOf("Square hips", "Breathe into the stretch"),
                        ),
                        ExerciseTemplate(
                            "Seated twist",
                            BRIEF,
                            listOf("Lengthen spine before twisting", "Both sides"),
                        ),
                    ),
                MovementType.BALANCE to
                    listOf(
                        ExerciseTemplate(
                            "Single leg stand",
                            QUICK,
                            listOf("Engage core", "Soft knee"),
                        ),
                        ExerciseTemplate(
                            "Tree pose",
                            BRIEF,
                            listOf("Foot on thigh or calf", "Drishti (focused gaze)"),
                        ),
                        ExerciseTemplate(
                            "Tandem stance",
                            QUICK,
                            listOf("Heel to toe", "Steady breath"),
                        ),
                    ),
                MovementType.RECOVERY to
                    listOf(
                        ExerciseTemplate(
                            "Child's pose",
                            MEDIUM,
                            listOf("Knees wide", "Forehead to floor"),
                        ),
                        ExerciseTemplate(
                            "Legs up the wall",
                            LONG,
                            listOf("Relax fully", "Breathe deeply"),
                        ),
                        ExerciseTemplate(
                            "Reclined twist",
                            SHORT,
                            listOf("Knees to chest", "Drop to one side"),
                        ),
                    ),
                MovementType.BACK_PAIN_RELIEF to
                    listOf(
                        ExerciseTemplate(
                            "Knees to chest",
                            SHORT,
                            listOf("Slow controlled motion", "Breathe deeply"),
                        ),
                        ExerciseTemplate(
                            "Cat-cow",
                            SHORT,
                            listOf("Inhale arching", "Exhale rounding"),
                        ),
                        ExerciseTemplate(
                            "Sphinx pose",
                            MEDIUM,
                            listOf("Press forearms down", "Lift chest gently"),
                        ),
                    ),
            )
    }
}
