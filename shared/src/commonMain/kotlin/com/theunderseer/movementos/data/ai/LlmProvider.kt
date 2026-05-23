package com.theunderseer.movementos.data.ai

/**
 * Available LLM providers.
 *
 * Order in enum reflects fallback priority (Gemini first — best free tier).
 * Orchestrator iterates in declaration order on failure.
 */
enum class LlmProvider {
    GEMINI,
    ANTHROPIC,
    OPENAI,
}
