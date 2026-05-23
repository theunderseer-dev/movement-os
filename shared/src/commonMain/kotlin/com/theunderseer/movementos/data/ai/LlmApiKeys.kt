package com.theunderseer.movementos.data.ai

/**
 * API keys for LLM providers.
 *
 * Injected by platform code (Android: from BuildConfig, iOS: from Info.plist).
 * Adapters consume specific keys via Koin injection.
 *
 * Empty string indicates missing key. Adapter will fail with Unauthorized
 * which orchestrator handles by skipping the provider.
 */
data class LlmApiKeys(
    val geminiApiKey: String,
    val anthropicApiKey: String,
    val openAiApiKey: String,
)
