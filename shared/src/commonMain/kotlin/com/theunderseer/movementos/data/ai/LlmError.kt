package com.theunderseer.movementos.data.ai

/**
 * Typed errors from LLM calls.
 */
sealed class LlmError {
    data object RateLimited : LlmError()

    data object Unauthorized : LlmError()

    data object Timeout : LlmError()

    data object Network : LlmError()

    data class InvalidResponse(
        val message: String,
    ) : LlmError()

    data class ProviderError(
        val message: String,
        val statusCode: Int? = null,
    ) : LlmError()

    data object NoProvidersAvailable : LlmError()

    data class Unknown(
        val message: String?,
    ) : LlmError()
}
