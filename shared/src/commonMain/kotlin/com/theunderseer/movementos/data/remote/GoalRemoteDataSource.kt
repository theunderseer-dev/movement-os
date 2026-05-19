package com.theunderseer.movementos.data.remote

import com.theunderseer.movementos.domain.model.UserGoal

internal interface GoalRemoteDataSource {
    suspend fun getCurrent(): UserGoal?

    suspend fun getById(id: String): UserGoal?

    suspend fun upsert(goal: UserGoal)
}

internal class StubGoalRemoteDataSource : GoalRemoteDataSource {
    override suspend fun getCurrent(): UserGoal? = null

    override suspend fun getById(id: String): UserGoal? = null

    override suspend fun upsert(goal: UserGoal) = Unit
}
