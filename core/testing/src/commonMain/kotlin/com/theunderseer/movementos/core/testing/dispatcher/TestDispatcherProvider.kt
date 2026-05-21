package com.theunderseer.movementos.core.testing.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * Creates dispatchers for tests.
 *
 * Use [unconfined] for use case tests — runs coroutines eagerly, simpler assertions.
 * Use [standard] when test needs explicit time advancement via testScheduler.
 */
object TestDispatcherProvider {
    fun unconfined(): CoroutineDispatcher = UnconfinedTestDispatcher()

    fun standard(scope: TestScope): CoroutineDispatcher = StandardTestDispatcher(scope.testScheduler)
}
