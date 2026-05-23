package com.theunderseer.movementos.data.ai.adapter

import com.theunderseer.movementos.data.ai.LlmProvider
import com.theunderseer.movementos.data.ai.LlmRequest
import com.theunderseer.movementos.data.ai.LlmResponse
import com.theunderseer.movementos.data.network.ApiResult

/**
 * Provider-specific adapter. Each implementation knows how to:
 * - Map LlmRequest → provider request schema
 * - Call provider HTTP endpoint
 * - Map response → LlmResponse
 * - Map errors → ApiResult.Error.* (via safeApiCall)
 */
interface LlmAdapter {
    val provider: LlmProvider

    suspend fun complete(request: LlmRequest): ApiResult<LlmResponse>
}
