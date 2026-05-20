package com.theunderseer.movementos.data.network.llm

import com.theunderseer.movementos.data.network.ApiResult
import com.theunderseer.movementos.data.network.llm.dto.LlmCompletionRequest
import com.theunderseer.movementos.data.network.llm.dto.LlmCompletionResponse
import com.theunderseer.movementos.data.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

internal class LlmApiClientKtorImpl(
    private val httpClient: HttpClient,
) : LlmApiClient {
    override suspend fun generateCompletion(request: LlmCompletionRequest): ApiResult<LlmCompletionResponse> =
        safeApiCall {
            httpClient
                .post("/v1/completions") {
                    setBody(request)
                }.body()
        }
}
