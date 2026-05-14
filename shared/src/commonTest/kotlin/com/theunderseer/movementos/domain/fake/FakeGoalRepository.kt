package com.theunderseer.movementos.domain.fake

import com.theunderseer.movementos.domain.model.UserGoal
import com.theunderseer.movementos.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory fake of [GoalRepository] for testing.
 *
 * Behaviour mirrors a real implementation: setting a goal replaces any previous one,
 * and observers see the change reactively.
 */
class FakeGoalRepository : GoalRepository {
    private val goals = mutableMapOf<String, UserGoal>()
    private val currentGoalFlow = MutableStateFlow<UserGoal?>(null)

    override fun observeCurrentGoal(): Flow<UserGoal?> = currentGoalFlow

    override suspend fun getById(id: String): UserGoal? = goals[id]

    override suspend fun save(goal: UserGoal) {
        goals[goal.id] = goal
        currentGoalFlow.value = goal
    }
}
