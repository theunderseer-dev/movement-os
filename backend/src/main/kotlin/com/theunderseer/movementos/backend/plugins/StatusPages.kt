package com.theunderseer.movementos.backend.plugins

import com.theunderseer.movementos.backend.common.ApiError
import com.theunderseer.movementos.backend.common.exceptions.AppException
import com.theunderseer.movementos.backend.common.exceptions.ErrorCode
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callid.callId
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory

/**
 * Centralized error handling — maps exceptions to structured [ApiError].
 *
 * AppException subtypes → their declared code/status.
 * RequestValidationException → VALIDATION_FAILED.
 * Anything else → INTERNAL_ERROR (logged with stacktrace, generic message to client).
 *
 * Never leaks internal exception details (stacktraces, SQL errors) to clients.
 */
fun Application.configureStatusPages() {
    val logger = LoggerFactory.getLogger("StatusPages")

    install(StatusPages) {
        exception<AppException> { call, cause ->
            call.respond(
                cause.status,
                ApiError(
                    code = cause.code,
                    message = cause.message,
                    details = cause.details,
                    requestId = call.callId,
                ),
            )
        }

        exception<RequestValidationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiError(
                    code = ErrorCode.VALIDATION_FAILED,
                    message = cause.reasons.joinToString("; "),
                    requestId = call.callId,
                ),
            )
        }

        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception [requestId=${call.callId}]", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiError(
                    code = ErrorCode.INTERNAL_ERROR,
                    message = "An unexpected error occurred",
                    requestId = call.callId,
                ),
            )
        }
    }
}
