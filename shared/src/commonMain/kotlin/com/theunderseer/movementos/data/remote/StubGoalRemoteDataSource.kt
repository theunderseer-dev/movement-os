package com.theunderseer.movementos.data.remote

import com.theunderseer.movementos.domain.model.UserGoal

internal class StubGoalRemoteDataSource : GoalRemoteDataSource {
    override suspend fun getCurrent(): UserGoal? = null

    override suspend fun upsert(goal: UserGoal) = Unit
}
