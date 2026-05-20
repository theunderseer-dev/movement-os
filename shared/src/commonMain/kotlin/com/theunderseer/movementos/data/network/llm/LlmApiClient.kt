package com.theunderseer.movementos.data.network.llm

import com.theunderseer.movementos.data.network.ApiResult
import com.theunderseer.movementos.data.network.llm.dto.LlmCompletionRequest
import com.theunderseer.movementos.data.network.llm.dto.LlmCompletionResponse

/**
 * LLM API client interface.
 *
 * Single abstraction lets [com.theunderseer.movementos.domain.usecase.ProgramGenerator]
 * swap providers without touching domain.
 */
interface LlmApiClient {
    suspend fun generateCompletion(request: LlmCompletionRequest): ApiResult<LlmCompletionResponse>
}
