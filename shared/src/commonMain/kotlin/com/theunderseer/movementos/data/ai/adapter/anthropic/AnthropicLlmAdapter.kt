package com.theunderseer.movementos.data.ai.adapter.anthropic

import com.theunderseer.movementos.data.ai.LlmProvider
import com.theunderseer.movementos.data.ai.LlmRequest
import com.theunderseer.movementos.data.ai.LlmResponse
import com.theunderseer.movementos.data.ai.TokenUsage
import com.theunderseer.movementos.data.ai.adapter.LlmAdapter
import com.theunderseer.movementos.data.ai.adapter.anthropic.model.AnthropicMessage
import com.theunderseer.movementos.data.ai.adapter.anthropic.model.AnthropicRequest
import com.theunderseer.movementos.data.ai.adapter.anthropic.model.AnthropicResponse
import com.theunderseer.movementos.data.network.ApiResult
import com.theunderseer.movementos.data.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class AnthropicLlmAdapter(
    private val httpClient: HttpClient,
    private val apiKey: String,
    private val model: String = DEFAULT_MODEL,
) : LlmAdapter {
    override val provider: LlmProvider = LlmProvider.ANTHROPIC

    override suspend fun complete(request: LlmRequest): ApiResult<LlmResponse> =
        safeApiCall {
            val response =
                httpClient
                    .post("https://api.anthropic.com/v1/messages") {
                        header("x-api-key", apiKey)
                        header("anthropic-version", "2023-06-01")
                        contentType(ContentType.Application.Json)
                        setBody(
                            AnthropicRequest(
                                model = model,
                                system = request.systemPrompt,
                                messages = listOf(
                                    AnthropicMessage(
                                        role = "user",
                                        content = request.userPrompt
                                    )
                                ),
                                maxTokens = request.maxTokens,
                                temperature = request.temperature,
                            ),
                        )
                    }.body<AnthropicResponse>()

            LlmResponse(
                content =
                    response.content.firstOrNull()?.text
                        ?: throw IllegalStateException("Anthropic returned no content"),
                provider = LlmProvider.ANTHROPIC,
                model = response.model,
                usage =
                    TokenUsage(
                        promptTokens = response.usage.inputTokens,
                        completionTokens = response.usage.outputTokens,
                        totalTokens = response.usage.inputTokens + response.usage.outputTokens,
                    ),
            )
        }

    private companion object {
        const val DEFAULT_MODEL = "claude-haiku-4-5-20251001"
    }
}
