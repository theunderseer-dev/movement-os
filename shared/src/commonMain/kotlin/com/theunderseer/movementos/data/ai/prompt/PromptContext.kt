package com.theunderseer.movementos.data.ai.prompt

/**
 * Context values for filling a [PromptTemplate].
 *
 * Use [Builder] for type-safe construction:
 * ```
 * val context = PromptContext.Builder()
 *     .put("focus", goal.focus.name)
 *     .put("sessions_per_week", goal.sessionsPerWeek.toString())
 *     .build()
 * ```
 */
@ConsistentCopyVisibility
data class PromptContext internal constructor(
    val values: Map<String, String>,
) {
    class Builder {
        private val values = mutableMapOf<String, String>()

        fun put(
            key: String,
            value: String,
        ): Builder = apply { values[key] = value }

        fun put(
            key: String,
            value: Int,
        ): Builder = put(key, value.toString())

        fun put(
            key: String,
            value: Number,
        ): Builder = put(key, value.toString())

        fun put(
            key: String,
            value: Boolean,
        ): Builder = put(key, value.toString())

        fun put(
            key: String,
            value: List<String>,
        ): Builder = put(key, value.joinToString(", "))

        fun build(): PromptContext = PromptContext(values.toMap())
    }

    companion object {
        val EMPTY = PromptContext(emptyMap())
    }
}
