package com.theunderseer.movementos.data.orchestration

/**
 * Remote source returned no data for a resource that was expected to exist.
 *
 * Mapped to [DataError.NotFound] by repositories.
 */
internal class RemoteNotFoundException(
    resource: String,
) : RuntimeException("Remote returned no data for: $resource")

/**
 * Remote operation failed for reasons that should not propagate to UI as crash.
 * Wraps underlying Ktor/network exceptions with domain-meaningful semantics.
 */
internal class RemoteUnavailableException(
    cause: Throwable,
) : RuntimeException("Remote unavailable", cause)
