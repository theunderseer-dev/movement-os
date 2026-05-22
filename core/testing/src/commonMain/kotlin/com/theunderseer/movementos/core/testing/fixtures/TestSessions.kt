package com.theunderseer.movementos.core.testing.fixtures

import com.theunderseer.movementos.domain.model.Exercise
import com.theunderseer.movementos.domain.model.Session
import com.theunderseer.movementos.domain.model.values.Duration

object TestSessions {
    fun aSession(
        id: String = "session-1",
        programId: String = "program-1",
        orderIndex: Int = 0,
        exercises: List<Exercise> = listOf(TestExercises.anExercise()),
        estimatedDuration: Duration = Duration.ofMinutes(20),
    ): Session =
        Session(
            id = id,
            programId = programId,
            orderIndex = orderIndex,
            exercises = exercises,
            estimatedDuration = estimatedDuration,
        )
}
