package com.theunderseer.movementos.data.repository

import com.theunderseer.movementos.data.local.GoalLocalDataSource
import com.theunderseer.movementos.domain.model.UserGoal
import com.theunderseer.movementos.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow

internal class DefaultGoalRepository(
    private val local: GoalLocalDataSource,
) : GoalRepository {
    override fun observeCurrentGoal(): Flow<UserGoal?> = local.observeCurrent()

    override suspend fun getById(id: String): UserGoal? = local.getById(id)

    override suspend fun save(goal: UserGoal) = local.save(goal)
}
