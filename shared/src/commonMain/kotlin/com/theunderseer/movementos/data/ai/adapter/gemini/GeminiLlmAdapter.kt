package com.theunderseer.movementos.data.ai.adapter.gemini

import com.theunderseer.movementos.data.ai.LlmProvider
import com.theunderseer.movementos.data.ai.LlmRequest
import com.theunderseer.movementos.data.ai.LlmResponse
import com.theunderseer.movementos.data.ai.TokenUsage
import com.theunderseer.movementos.data.ai.adapter.LlmAdapter
import com.theunderseer.movementos.data.ai.adapter.gemini.dto.GeminiContent
import com.theunderseer.movementos.data.ai.adapter.gemini.dto.GeminiGenerationConfig
import com.theunderseer.movementos.data.ai.adapter.gemini.dto.GeminiPart
import com.theunderseer.movementos.data.ai.adapter.gemini.dto.GeminiRequest
import com.theunderseer.movementos.data.ai.adapter.gemini.dto.GeminiResponse
import com.theunderseer.movementos.data.network.ApiResult
import com.theunderseer.movementos.data.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Adapter for Google Gemini API.
 *
 * Endpoint: https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent
 * Auth: API key as query parameter (?key=...)
 *
 * Free tier: 15 req/min, 1500 req/day for Flash models.
 */
internal class GeminiLlmAdapter(
    private val httpClient: HttpClient,
    private val apiKey: String,
    private val model: String = DEFAULT_MODEL,
) : LlmAdapter {
    override val provider: LlmProvider = LlmProvider.GEMINI

    override suspend fun complete(request: LlmRequest): ApiResult<LlmResponse> =
        safeApiCall {
            val url =
                buildString {
                    append("https://generativelanguage.googleapis.com/v1beta/models/")
                    append(model)
                    append(":generateContent?key=")
                    append(apiKey)
                }

            val response =
                httpClient
                    .post(url) {
                        contentType(ContentType.Application.Json)
                        setBody(request.toGeminiRequest())
                    }.body<GeminiResponse>()

            response.toLlmResponse(model = model)
        }

    private fun LlmRequest.toGeminiRequest(): GeminiRequest =
        GeminiRequest(
            contents =
                listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = userPrompt)),
                    ),
                ),
            generationConfig =
                GeminiGenerationConfig(
                    temperature = temperature,
                    maxOutputTokens = maxTokens,
                    responseMimeType =
                        if (responseFormat == LlmRequest.ResponseFormat.JSON) {
                            "application/json"
                        } else {
                            null
                        },
                ),
            systemInstruction =
                systemPrompt?.let {
                    GeminiContent(parts = listOf(GeminiPart(text = it)))
                },
        )

    private fun GeminiResponse.toLlmResponse(model: String): LlmResponse {
        val content =
            candidates
                .firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?: error("Gemini returned no candidates")
        return LlmResponse(
            content = content,
            provider = LlmProvider.GEMINI,
            model = model,
            usage =
                TokenUsage(
                    promptTokens = usageMetadata?.promptTokenCount ?: 0,
                    completionTokens = usageMetadata?.candidatesTokenCount ?: 0,
                    totalTokens = usageMetadata?.totalTokenCount ?: 0,
                ),
        )
    }

    private companion object {
        const val DEFAULT_MODEL = "gemini-2.0-flash"
    }
}
