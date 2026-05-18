package com.theunderseer.movementos.domain.common

/**
 * Typed errors flowing from data sources to UI.
 *
 * Specific enough for UI to render meaningful messages, generic enough to
 * avoid coupling to Ktor/SQLDelight error types.
 */
sealed class DataError {
    data object Network : DataError()

    data object Unauthorized : DataError()

    data object NotFound : DataError()

    data object Local : DataError()

    data class Unknown(
        val message: String?,
    ) : DataError()
}
