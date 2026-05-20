package com.theunderseer.movementos.data.network

import co.touchlab.kermit.Logger
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import io.ktor.utils.io.errors.IOException

private val logger = Logger.withTag("SafeApiCall")

/**
 * Wraps a network call, converting all exceptions to [ApiResult] subtypes.
 *
 * Repositories use this, never raw try/catch. Errors are typed and exhaustively
 * handlable via `when` on [ApiResult].
 */
internal suspend fun <T> safeApiCall(block: suspend () -> T): ApiResult<T> =
    try {
        ApiResult.Success(block())
    } catch (e: ClientRequestException) {
        val code = e.response.status.value
        logger.w(e) { "Client error $code" }
        when (e.response.status) {
            HttpStatusCode.Unauthorized -> ApiResult.Error.Unauthorized
            else -> ApiResult.Error.HttpError(code, e.message)
        }
    } catch (e: ServerResponseException) {
        logger.w(e) { "Server error ${e.response.status.value}" }
        ApiResult.Error.HttpError(e.response.status.value, e.message)
    } catch (e: ConnectTimeoutException) {
        logger.w(e) { "Connect timeout" }
        ApiResult.Error.Timeout
    } catch (e: SocketTimeoutException) {
        logger.w(e) { "Socket timeout" }
        ApiResult.Error.Timeout
    } catch (e: IOException) {
        logger.w(e) { "Network IO error" }
        ApiResult.Error.Network
    } catch (e: JsonConvertException) {
        logger.e(e) { "Serialization error" }
        ApiResult.Error.Serialization(e.message)
    } catch (
        @Suppress("TooGenericExceptionCaught") e: Exception,
    ) {
        logger.e(e) { "Unknown error" }
        ApiResult.Error.Unknown(e.message)
    }
