package com.theunderseer.movementos.data.ai.prompt

/**
 * Errors from validating and parsing LLM responses.
 */
sealed class ResponseValidationError {
    data class InvalidJson(
        val message: String,
        val rawContent: String,
    ) : ResponseValidationError()

    data class SchemaViolation(
        val field: String,
        val reason: String,
    ) : ResponseValidationError()

    data class MissingRequiredField(
        val field: String,
    ) : ResponseValidationError()

    data class UnexpectedValue(
        val field: String,
        val value: String,
        val expected: String,
    ) : ResponseValidationError()

    data class Unknown(
        val message: String,
    ) : ResponseValidationError()
}
