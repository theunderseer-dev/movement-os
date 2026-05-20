package com.theunderseer.movementos.data.network.remote

import ProgramDto
import com.theunderseer.movementos.data.network.ApiResult
import com.theunderseer.movementos.data.network.HttpStatusCodes
import com.theunderseer.movementos.data.network.remote.dto.mappers.toDomain
import com.theunderseer.movementos.data.network.remote.dto.mappers.toDto
import com.theunderseer.movementos.data.network.safeApiCall
import com.theunderseer.movementos.data.remote.ProgramRemoteDataSource
import com.theunderseer.movementos.domain.model.Program
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

internal class ProgramRemoteDataSourceKtorImpl(
    private val httpClient: HttpClient,
) : ProgramRemoteDataSource {
    override suspend fun getActive(): Program? =
        when (val result = safeApiCall { httpClient.get("/programs/active").body<ProgramDto>() }) {
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

    override suspend fun upsert(program: Program) {
        safeApiCall { httpClient.post("/programs") { setBody(program.toDto()) } }
    }
}
