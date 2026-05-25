package com.theunderseer.movementos.data.ai.prompt.program

import com.theunderseer.movementos.data.ai.prompt.PromptTemplate
import com.theunderseer.movementos.domain.model.UserGoal
import com.theunderseer.movementos.domain.model.values.MovementType

/**
 * Prompt template for generating personalized movement programs.
 *
 * Constraints embedded in system prompt:
 * - Output strict JSON matching schema
 * - Use only declared MovementType values
 * - Session duration aligns with user's available time
 * - Number of sessions matches user's weekly frequency
 *
 * Schema is included in system prompt so LLM has explicit contract.
 */
internal object ProgramGenerationPrompt {
    val template =
        PromptTemplate(
            systemPrompt = SYSTEM_PROMPT,
            userPromptTemplate = USER_TEMPLATE,
        )

    fun contextFrom(goal: UserGoal): com.theunderseer.movementos.data.ai.prompt.PromptContext =
        com.theunderseer.movementos.data.ai.prompt.PromptContext
            .Builder()
            .put("goal_description", goal.description)
            .put("focus", goal.focus.name)
            .put("sessions_per_week", goal.sessionsPerWeek)
            .put("session_minutes", goal.timePerSession.minutes)
            .put("movement_types", MovementType.entries.joinToString(", ") { it.name })
            .build()

    private const val SYSTEM_PROMPT = """
        You are a movement coach generating personalized training programs.

        Constraints:
        - Output MUST be valid JSON matching the schema below
        - Use only the declared MovementType enum values
        - Each session duration must approximately match the user's available time
        - Generate exactly the number of sessions matching the user's weekly frequency
        - Exercises should progress in difficulty across sessions
        - Include 2-3 verbal cues per exercise to guide form

        JSON Schema:
        {
          "name": "string (program title, max 80 chars)",
          "description": "string (1-2 sentences explaining the approach)",
          "primary_type": "MovementType enum",
          "sessions": [
            {
              "order_index": "integer (0-based)",
              "estimated_duration_minutes": "integer",
              "exercises": [
                {
                  "name": "string (exercise name)",
                  "duration_seconds": "integer",
                  "type": "MovementType enum",
                  "cues": ["string", ...]
                }
              ]
            }
          ]
        }

        Do not include explanatory text, markdown, or commentary outside JSON.
        Respond with the JSON object only.
    """

    private const val USER_TEMPLATE = """
        Generate a movement program for the following goal:

        Goal: {{goal_description}}
        Focus area: {{focus}}
        Sessions per week: {{sessions_per_week}}
        Available time per session: {{session_minutes}} minutes

        Allowed MovementType values: {{movement_types}}

        Generate the program now as JSON.
    """
}
