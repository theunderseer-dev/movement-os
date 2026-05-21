package com.theunderseer.movementos.core.testing.fixtures

import com.theunderseer.movementos.domain.model.Exercise
import com.theunderseer.movementos.domain.model.values.Duration
import com.theunderseer.movementos.domain.model.values.MovementType

object TestExercises {
    fun anExercise(
        id: String = "exercise-1",
        name: String = "Cat-cow",
        duration: Duration = Duration.ofMinutes(2),
        type: MovementType = MovementType.MOBILITY,
        cues: List<String> = emptyList(),
    ): Exercise =
        Exercise(
            id = id,
            name = name,
            duration = duration,
            type = type,
            cues = cues,
        )
}
