package com.theunderseer.movementos.data.ai.prompt

/**
 * Reusable prompt template with named placeholders.
 *
 * Templates are immutable and validated at construction. Fail fast if syntax wrong.
 * Use [render] to fill placeholders with concrete values per request.
 *
 * Placeholder syntax: {{name}} — surrounded by double braces, lowercase + underscores.
 */
data class PromptTemplate(
    val systemPrompt: String?,
    val userPromptTemplate: String,
) {
    private val placeholders: Set<String> = PLACEHOLDER_REGEX
        .findAll(userPromptTemplate)
        .map { it.groupValues[1] }
        .toSet()

    /**
     * Renders the template with the given context values.
     *
     * @throws IllegalArgumentException if any required placeholder is missing from [context]
     */
    fun render(context: PromptContext): RenderedPrompt {
        val missing = placeholders - context.values.keys
        require(missing.isEmpty()) { "Missing placeholders: $missing" }

        val rendered = placeholders.fold(userPromptTemplate) { acc, name ->
            acc.replace("{{$name}}", context.values.getValue(name))
        }

        return RenderedPrompt(systemPrompt = systemPrompt, userPrompt = rendered)
    }

    private companion object {
        val PLACEHOLDER_REGEX = """\{\{([a-z_][a-z0-9_]*)\}\}""".toRegex()
    }
}

data class RenderedPrompt(
    val systemPrompt: String?,
    val userPrompt: String,
)
