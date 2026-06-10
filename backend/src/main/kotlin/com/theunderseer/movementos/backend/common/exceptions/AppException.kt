package com.theunderseer.movementos.backend.common.exceptions

import io.ktor.http.HttpStatusCode

/**
 * Base application exception carrying an [ErrorCode] and HTTP status.
 *
 * StatusPages plugin maps these to structured [ApiError] responses.
 * Throw subtypes from routes/services; never leak raw exceptions to clients.
 */
sealed class AppException(
    val code: ErrorCode,
    val status: HttpStatusCode,
    override val message: String,
    val details: Map<String, String> = emptyMap(),
) : RuntimeException(message) {
    class ValidationException(
        message: String,
        details: Map<String, String> = emptyMap(),
    ) : AppException(ErrorCode.VALIDATION_FAILED, HttpStatusCode.BadRequest, message, details)

    class UnauthorizedException(
        message: String = "Authentication required",
    ) : AppException(ErrorCode.UNAUTHORIZED, HttpStatusCode.Unauthorized, message)

    class ForbiddenException(
        message: String = "Access denied",
    ) : AppException(ErrorCode.FORBIDDEN, HttpStatusCode.Forbidden, message)

    class NotFoundException(
        message: String = "Resource not found",
    ) : AppException(ErrorCode.NOT_FOUND, HttpStatusCode.NotFound, message)

    class ConflictException(
        message: String,
    ) : AppException(ErrorCode.CONFLICT, HttpStatusCode.Conflict, message)

    class RateLimitedException(
        message: String = "Too many requests",
    ) : AppException(ErrorCode.RATE_LIMITED, HttpStatusCode.TooManyRequests, message)

    class ServiceUnavailableException(
        message: String = "Service temporarily unavailable",
    ) : AppException(ErrorCode.SERVICE_UNAVAILABLE, HttpStatusCode.ServiceUnavailable, message)
}
