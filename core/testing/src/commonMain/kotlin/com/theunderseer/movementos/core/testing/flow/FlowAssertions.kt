package com.theunderseer.movementos.core.testing.flow

import app.cash.turbine.ReceiveTurbine
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Helpers for common Turbine assertion patterns.
 *
 * Use inside .test {} blocks:
 * ```
 * flow.test {
 *     awaitItemOfType<Success<Program>>().data.id shouldBe "program-1"
 * }
 * ```
 */
suspend inline fun <reified T> ReceiveTurbine<*>.awaitItemOfType(): T {
    val item = awaitItem()
    assertIs<T>(item)
    return item
}

suspend fun <T> ReceiveTurbine<T>.awaitSpecific(expected: T) {
    val actual = awaitItem()
    assertEquals(expected, actual)
}
