package com.theunderseer.movementos.data.network.remote

import ProgressEntryDto
import com.theunderseer.movementos.data.network.ApiResult
import com.theunderseer.movementos.data.network.safeApiCall
import com.theunderseer.movementos.data.remote.SessionRemoteDataSource
import com.theunderseer.movementos.domain.model.ProgressEntry
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import toDomain
import toDto

internal class SessionRemoteDataSourceKtorImpl(
    private val httpClient: HttpClient,
) : SessionRemoteDataSource {
    override suspend fun recordCompletion(entry: ProgressEntry) {
        safeApiCall {
            httpClient.post("/sessions/completions") { setBody(entry.toDto()) }
        }
    }

    override suspend fun getProgressHistory(): List<ProgressEntry> =
        when (
            val result =
                safeApiCall { httpClient.get("/sessions/completions").body<List<ProgressEntryDto>>() }
        ) {
            is ApiResult.Success -> result.data.map { it.toDomain() }
            is ApiResult.Error -> throw RemoteException(result)
        }
}
