package com.theunderseer.movementos.data.ai.prompt

import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResponseParserTest {

    @Serializable
    data class TestPayload(val name: String, val value: Int)

    private val parser = ResponseParser()

    @Test
    fun `parses clean JSON`() {
        val content = """{"name": "test", "value": 42}"""
        val result = parser.parse(content, TestPayload.serializer())
        assertTrue(result.isSuccess)
        assertEquals("test", result.getOrNull()?.name)
        assertEquals(42, result.getOrNull()?.value)
    }

    @Test
    fun `extracts JSON from markdown code fence`() {
        val content = """
            Here is your data:
            ```json
            {"name": "test", "value": 42}
            ```
        """.trimIndent()

        val result = parser.parse(content, TestPayload.serializer())
        assertTrue(result.isSuccess)
        assertEquals("test", result.getOrNull()?.name)
    }

    @Test
    fun `extracts JSON from unfenced code block`() {
        val content = """
              {"name": "test", "value": 42}
              """.trimIndent()

        val result = parser.parse(content, TestPayload.serializer())
        assertTrue(result.isSuccess)
    }

    @Test
    fun `extracts JSON from leading prose`() {
        val content = "Here is the result: {\"name\": \"test\", \"value\": 42}"

        val result = parser.parse(content, TestPayload.serializer())
        assertTrue(result.isSuccess)
        assertEquals(42, result.getOrNull()?.value)
    }

    @Test
    fun `fails on malformed JSON`() {
        val content = """{"name": "test", "value":}"""
        val result = parser.parse(content, TestPayload.serializer())
        assertTrue(result.isFailure)
    }
}
