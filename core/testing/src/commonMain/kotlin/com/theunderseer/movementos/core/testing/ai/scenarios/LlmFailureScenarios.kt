package com.theunderseer.movementos.core.testing.ai.scenarios

import com.theunderseer.movementos.data.network.ApiResult

/**
 * Canned failure scenarios for orchestrator and adapter testing.
 *
 * Use to set up FakeLlmClient / FakeLlmAdapter for failure-path tests.
 */
object LlmFailureScenarios {
    val rateLimited: ApiResult.Error = ApiResult.Error.HttpError(429, "Rate limit exceeded")
    val unauthorized: ApiResult.Error = ApiResult.Error.Unauthorized
    val serverError: ApiResult.Error = ApiResult.Error.HttpError(500, "Internal server error")
    val networkFailure: ApiResult.Error = ApiResult.Error.Network
    val timeout: ApiResult.Error = ApiResult.Error.Timeout
    val malformedResponse: ApiResult.Error = ApiResult.Error.Serialization("Invalid JSON")
    val unknown: ApiResult.Error = ApiResult.Error.Unknown("Provider returned empty response")
}
