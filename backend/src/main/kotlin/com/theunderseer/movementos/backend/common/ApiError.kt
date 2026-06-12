package com.theunderseer.movementos.backend.common

import com.theunderseer.movementos.backend.common.exceptions.ErrorCode
import kotlinx.serialization.Serializable

/**
 * Structured error response returned to clients.
 *
 * `requestId` lets clients reference a specific failed request when reporting
 * issues — correlates with server logs.
 */
@Serializable
data class ApiError(
    val code: ErrorCode,
    val message: String,
    val details: Map<String, String> = emptyMap(),
    val requestId: String? = null,
)
