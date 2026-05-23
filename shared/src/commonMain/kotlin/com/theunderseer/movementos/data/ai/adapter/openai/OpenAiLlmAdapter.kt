package com.theunderseer.movementos.data.ai.adapter.openai

import com.theunderseer.movementos.data.ai.LlmProvider
import com.theunderseer.movementos.data.ai.LlmRequest
import com.theunderseer.movementos.data.ai.LlmResponse
import com.theunderseer.movementos.data.ai.TokenUsage
import com.theunderseer.movementos.data.ai.adapter.LlmAdapter
import com.theunderseer.movementos.data.ai.adapter.openai.dto.OpenAiMessage
import com.theunderseer.movementos.data.ai.adapter.openai.dto.OpenAiRequest
import com.theunderseer.movementos.data.ai.adapter.openai.dto.OpenAiResponse
import com.theunderseer.movementos.data.ai.adapter.openai.dto.OpenAiResponseFormat
import com.theunderseer.movementos.data.network.ApiResult
import com.theunderseer.movementos.data.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class OpenAiLlmAdapter(
    private val httpClient: HttpClient,
    private val apiKey: String,
    private val model: String = DEFAULT_MODEL,
) : LlmAdapter {
    override val provider: LlmProvider = LlmProvider.OPENAI

    override suspend fun complete(request: LlmRequest): ApiResult<LlmResponse> =
        safeApiCall {
            val messages =
                buildList {
                    request.systemPrompt?.let { add(OpenAiMessage(role = "system", content = it)) }
                    add(OpenAiMessage(role = "user", content = request.userPrompt))
                }

            val response =
                httpClient
                    .post("https://api.openai.com/v1/chat/completions") {
                        header("Authorization", "Bearer $apiKey")
                        contentType(ContentType.Application.Json)
                        setBody(
                            OpenAiRequest(
                                model = model,
                                messages = messages,
                                maxTokens = request.maxTokens,
                                temperature = request.temperature,
                                responseFormat =
                                    if (request.responseFormat == LlmRequest.ResponseFormat.JSON) {
                                        OpenAiResponseFormat(type = "json_object")
                                    } else {
                                        null
                                    },
                            ),
                        )
                    }.body<OpenAiResponse>()

            LlmResponse(
                content =
                    response.choices
                        .firstOrNull()
                        ?.message
                        ?.content
                        ?: throw IllegalStateException("OpenAI returned no choices"),
                provider = LlmProvider.OPENAI,
                model = response.model,
                usage =
                    TokenUsage(
                        promptTokens = response.usage.promptTokens,
                        completionTokens = response.usage.completionTokens,
                        totalTokens = response.usage.totalTokens,
                    ),
            )
        }

    private companion object {
        const val DEFAULT_MODEL = "gpt-4o-mini"
    }
}
