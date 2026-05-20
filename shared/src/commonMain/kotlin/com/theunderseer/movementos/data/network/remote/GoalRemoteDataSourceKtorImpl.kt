package com.theunderseer.movementos.data.network.remote

import UserGoalDto
import com.theunderseer.movementos.data.network.ApiResult
import com.theunderseer.movementos.data.network.HttpStatusCodes
import com.theunderseer.movementos.data.network.safeApiCall
import com.theunderseer.movementos.data.remote.GoalRemoteDataSource
import com.theunderseer.movementos.domain.model.UserGoal
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import toDomain
import toDto

internal class GoalRemoteDataSourceKtorImpl(
    private val httpClient: HttpClient,
) : GoalRemoteDataSource {
    override suspend fun getCurrent(): UserGoal? =
        when (val result = safeApiCall { httpClient.get("/goals/current").body<UserGoalDto>() }) {
            is ApiResult.Success -> {
                result.data.toDomain()
            }

            is ApiResult.Error -> {
                if (result is ApiResult.Error.HttpError && result.code == HttpStatusCodes.NOT_FOUND) {
                    null
                } else {
                    throw RemoteException(result)
                }
            }
        }

    override suspend fun upsert(goal: UserGoal) {
        safeApiCall { httpClient.post("/goals") { setBody(goal.toDto()) } }
    }
}
