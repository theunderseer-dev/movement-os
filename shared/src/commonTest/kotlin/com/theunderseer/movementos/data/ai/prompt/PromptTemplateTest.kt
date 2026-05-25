package com.theunderseer.movementos.data.ai.prompt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PromptTemplateTest {
    @Test
    fun `renders template with all placeholders filled`() {
        val template =
            PromptTemplate(
                systemPrompt = "You are a coach",
                userPromptTemplate = "Goal: {{goal}} for {{sessions}} sessions",
            )
        val context =
            PromptContext
                .Builder()
                .put("goal", "improve mobility")
                .put("sessions", 3)
                .build()

        val rendered = template.render(context)

        assertEquals("You are a coach", rendered.systemPrompt)
        assertEquals("Goal: improve mobility for 3 sessions", rendered.userPrompt)
    }

    @Test
    fun `throws when required placeholder missing`() {
        val template =
            PromptTemplate(
                systemPrompt = null,
                userPromptTemplate = "Goal: {{goal}}",
            )
        val context = PromptContext.Builder().build()

        assertFailsWith<IllegalArgumentException> { template.render(context) }
    }

    @Test
    fun `handles repeated placeholders`() {
        val template =
            PromptTemplate(
                systemPrompt = null,
                userPromptTemplate = "{{name}} likes {{name}}",
            )
        val context = PromptContext.Builder().put("name", "Alice").build()

        val rendered = template.render(context)

        assertEquals("Alice likes Alice", rendered.userPrompt)
    }
}
