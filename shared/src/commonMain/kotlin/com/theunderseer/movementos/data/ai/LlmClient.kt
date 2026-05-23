package com.theunderseer.movementos.data.ai

import com.theunderseer.movementos.data.network.ApiResult

/**
 * Facade for LLM operations. Domain code (use cases) depends on this, not adapters.
 *
 * Default implementation is [LlmOrchestrator] which routes through providers
 * with retry + fallback. Other implementations could include test stubs.
 */
interface LlmClient {
    suspend fun complete(request: LlmRequest): ApiResult<LlmResponse>
}
