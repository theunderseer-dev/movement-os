package com.theunderseer.movementos.backend.config

/**
 * Typed application configuration.
 *
 * Loaded from HOCON (application.conf + environment overlay) and environment
 * variables. Centralizes all config so no scattered `System.getenv` calls.
 *
 * Secrets (DB password, JWT secret, LLM keys) come from env vars, never committed.
 */
data class AppConfig(
    val environment: Environment,
    val server: ServerConfig,
    val database: DatabaseConfig,
    val llm: LlmConfig,
) {
    data class ServerConfig(
        val host: String,
        val port: Int,
    )

    data class DatabaseConfig(
        val url: String,
        val user: String,
        val password: String,
        val maxPoolSize: Int,
    )

    data class LlmConfig(
        val geminiApiKey: String,
        val anthropicApiKey: String,
        val openAiApiKey: String,
    )
}
