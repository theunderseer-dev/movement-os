package com.theunderseer.movementos.data.ai.prompt

import co.touchlab.kermit.Logger
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Parses LLM responses to typed contracts via kotlinx.serialization.
 *
 * LLMs sometimes return JSON wrapped in markdown code fences (```json\n...\n```)
 * or with leading prose ("Here is the program: {...}"). Parser strips these
 * before deserializing.
 *
 * Returns [Result] — repository code handles failure (e.g., trigger repair flow).
 */
internal class ResponseParser(
    private val json: Json = DEFAULT_JSON,
    private val logger: Logger = Logger.withTag("ResponseParser"),
) {
    fun <T> parse(
        content: String,
        deserializer: KSerializer<T>,
    ): Result<T> {
        val cleaned = extractJson(content)
        return runCatching {
            json.decodeFromString(deserializer, cleaned)
        }.onFailure { throwable ->
            when (throwable) {
                is SerializationException -> logger.w(throwable) { "Serialization failed; raw content: $cleaned" }
                else -> logger.e(throwable) { "Unexpected parse error" }
            }
        }
    }

    /**
     * Strips markdown code fences and surrounding prose to isolate JSON.
     *
     * Handles:
     * - ```json\n{...}\n```
     * - ```\n{...}\n```
     * - "Here is the JSON: {...}" (extracts {...} via brace matching)
     */
    private fun extractJson(content: String): String {
        val trimmed = content.trim()

        val fencedMatch = CODE_FENCE_REGEX.find(trimmed)
        if (fencedMatch != null) {
            return fencedMatch.groupValues[1].trim()
        }

        val firstBrace = trimmed.indexOf('{')
        val firstBracket = trimmed.indexOf('[')
        val start =
            when {
                firstBrace == -1 -> firstBracket
                firstBracket == -1 -> firstBrace
                else -> minOf(firstBrace, firstBracket)
            }

        if (start == -1) return trimmed

        val openChar = trimmed[start]
        val closeChar = if (openChar == '{') '}' else ']'
        val end = trimmed.lastIndexOf(closeChar)

        return if (end > start) trimmed.substring(start, end + 1) else trimmed
    }

    private companion object {
        val DEFAULT_JSON =
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            }
        val CODE_FENCE_REGEX = """```(?:json)?\s*([\s\S]*?)\s*```""".toRegex()
    }
}
