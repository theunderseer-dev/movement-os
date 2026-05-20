package com.theunderseer.movementos.data.remote

import com.theunderseer.movementos.domain.model.UserGoal

internal interface GoalRemoteDataSource {
    suspend fun getCurrent(): UserGoal?

    suspend fun upsert(goal: UserGoal)
}
