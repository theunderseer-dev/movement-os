package com.theunderseer.movementos.domain.fake

import com.theunderseer.movementos.domain.common.DataState
import com.theunderseer.movementos.domain.model.UserGoal
import com.theunderseer.movementos.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeGoalRepository : GoalRepository {
    private val goals = mutableMapOf<String, UserGoal>()
    private val currentGoalFlow = MutableStateFlow<UserGoal?>(null)

    override fun observeCurrentGoal(forceRefresh: Boolean): Flow<DataState<UserGoal?>> = currentGoalFlow.map { DataState.Success(it) }

    override suspend fun getById(id: String): UserGoal? = goals[id]

    override suspend fun save(goal: UserGoal) {
        goals[goal.id] = goal
        currentGoalFlow.value = goal
    }
}
