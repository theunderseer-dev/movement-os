package com.theunderseer.movementos.domain.repository

import com.theunderseer.movementos.domain.model.UserGoal
import kotlinx.coroutines.flow.Flow

/**
 * Manages user goals — what the user wants to achieve.
 */
interface GoalRepository {
    fun observeCurrentGoal(): Flow<UserGoal?>

    suspend fun getById(id: String): UserGoal?

    suspend fun save(goal: UserGoal)
}
