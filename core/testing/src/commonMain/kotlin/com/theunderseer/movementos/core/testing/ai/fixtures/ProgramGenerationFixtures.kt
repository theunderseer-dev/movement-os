package com.theunderseer.movementos.core.testing.ai.fixtures

/**
 * JSON payload fixtures for testing program generation parsing.
 *
 * Mirrors production schema in ProgramGenerationPrompt.SYSTEM_PROMPT.
 * Use [validProgramJson] for happy paths, [MALFORMED_JSON] / [missingFieldJson] / etc.
 * for error scenarios.
 */
object ProgramGenerationFixtures {
    val validProgramJson =
        """
        {
            "name": "10-Day Mobility Program",
            "description": "Foundational mobility work targeting hips and shoulders",
            "primary_type": "MOBILITY",
            "sessions": [
                {
                    "order_index": 0,
                    "estimated_duration_minutes": 20,
                    "exercises": [
                        {
                            "name": "Cat-cow",
                            "duration_seconds": 120,
                            "type": "MOBILITY",
                            "cues": ["Inhale arching back", "Exhale rounding spine"]
                        },
                        {
                            "name": "Hip circles",
                            "duration_seconds": 90,
                            "type": "MOBILITY",
                            "cues": ["Slow controlled", "Both directions"]
                        }
                    ]
                }
            ]
        }
        """.trimIndent()

    val validProgramJsonWithMarkdown =
        """
        Here is your program:
```json
        $validProgramJson
```
        """.trimIndent()

    val validProgramJsonWithProse = "I've generated the following program: $validProgramJson"

    const val MALFORMED_JSON = """{"name": "test", "description":}"""

    val missingFieldJson =
        """
        {
            "name": "Incomplete plan",
            "description": "Missing primary_type",
            "sessions": []
        }
        """.trimIndent()

    val invalidMovementTypeJson =
        """
        {
            "name": "Plan",
            "description": "Bad type",
            "primary_type": "DANCE_AEROBICS",
            "sessions": []
        }
        """.trimIndent()

    val emptySessionsJson =
        """
        {
            "name": "Empty plan",
            "description": "No sessions",
            "primary_type": "MOBILITY",
            "sessions": []
        }
        """.trimIndent()

    fun programJsonWith(
        name: String = "Test program",
        primaryType: String = "MOBILITY",
        sessionCount: Int = 1,
        exercisesPerSession: Int = 2,
    ): String {
        val sessions =
            (0 until sessionCount).joinToString(",\n") { sessionIdx ->
                val exercises =
                    (0 until exercisesPerSession).joinToString(",\n") { exerciseIdx ->
                        """
                        {
                            "name": "Exercise $exerciseIdx",
                            "duration_seconds": 60,
                            "type": "$primaryType",
                            "cues": ["cue1", "cue2"]
                        }
                        """.trimIndent()
                    }
                """
                {
                    "order_index": $sessionIdx,
                    "estimated_duration_minutes": 20,
                    "exercises": [$exercises]
                }
                """.trimIndent()
            }

        return """
            {
                "name": "$name",
                "description": "Generated test program",
                "primary_type": "$primaryType",
                "sessions": [$sessions]
            }
            """.trimIndent()
    }
}
