package com.theunderseer.movementos.data.ai.cache

import com.theunderseer.movementos.data.ai.LlmProvider
import com.theunderseer.movementos.data.ai.LlmResponse
import com.theunderseer.movementos.data.ai.TokenUsage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class CompositeLlmResponseCacheTest {
    private val l1 = InMemoryLlmResponseCache(maxSize = 5)
    private val l2 = InMemoryLlmResponseCache(maxSize = 50)
    private val cache = CompositeLlmResponseCache(l1 = l1, l2 = l2)

    private val testResponse =
        LlmResponse(
            content = "cached content",
            provider = LlmProvider.GEMINI,
            model = "gemini-2.0-flash",
            usage = TokenUsage(10, 20, 30),
        )

    @Test
    fun `put writes to both L1 and L2`() =
        runTest {
            cache.put("key-1", testResponse)

            assertNotNull(l1.get("key-1"))
            assertNotNull(l2.get("key-1"))
        }

    @Test
    fun `get returns from L1 when present`() =
        runTest {
            l1.put("key-1", testResponse)

            val result = cache.get("key-1")

            assertEquals(testResponse.content, result?.content)
        }

    @Test
    fun `get promotes from L2 to L1 on L1 miss`() =
        runTest {
            l2.put("key-1", testResponse)

            cache.get("key-1")

            assertNotNull(l1.get("key-1"))
        }

    @Test
    fun `get returns null when missing from both layers`() =
        runTest {
            val result = cache.get("missing")
            assertNull(result)
        }

    @Test
    fun `clear empties both layers`() =
        runTest {
            cache.put("key-1", testResponse)
            cache.clear()

            assertNull(l1.get("key-1"))
            assertNull(l2.get("key-1"))
        }
}
