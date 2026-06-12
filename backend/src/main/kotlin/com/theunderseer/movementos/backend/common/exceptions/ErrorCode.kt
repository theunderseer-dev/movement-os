package com.theunderseer.movementos.backend.common.exceptions

/**
 * Stable error codes for API clients.
 *
 * Clients switch on these codes (not HTTP status or message text) — codes are
 * part of the API contract and must not change once published.
 */
enum class ErrorCode {
    VALIDATION_FAILED,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    CONFLICT,
    RATE_LIMITED,
    INTERNAL_ERROR,
    SERVICE_UNAVAILABLE,
}
