package com.theunderseer.movementos.backend

import com.theunderseer.movementos.backend.testutil.runServerTest
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HealthRoutesTest {
    @Test
    fun `health returns 200 UP`() =
        runServerTest { client ->
            val response = client.get("/health")

            assertEquals(HttpStatusCode.Companion.OK, response.status)
            assertTrue(response.bodyAsText().contains("UP"))
        }

    @Test
    fun `ready returns 200 READY`() =
        runServerTest { client ->
            val response = client.get("/ready")

            assertEquals(HttpStatusCode.Companion.OK, response.status)
            assertTrue(response.bodyAsText().contains("READY"))
        }
}
