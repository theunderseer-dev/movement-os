package com.theunderseer.movementos.data.network

/**
 * Result wrapper for all network operations.
 *
 * Repositories receive [ApiResult], never raw exceptions.
 * UI consumers see [com.theunderseer.movementos.domain.common.DataError]
 * after repository mapping.
 *
 * Subtypes are exhaustive in `when`.
 * Compiler enforces complete handling.
 */
sealed class ApiResult<out T> {
    data class Success<T>(
        val data: T,
    ) : ApiResult<T>()

    sealed class Error : ApiResult<Nothing>() {
        data object Network : Error()

        data object Timeout : Error()

        data object Unauthorized : Error()

        data class HttpError(
            val code: Int,
            val message: String?,
        ) : Error()

        data class Serialization(
            val message: String?,
        ) : Error()

        data class Unknown(
            val message: String?,
        ) : Error()
    }
}
