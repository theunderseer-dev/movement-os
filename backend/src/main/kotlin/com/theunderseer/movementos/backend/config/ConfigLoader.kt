package com.theunderseer.movementos.backend.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

/**
 * Loads [AppConfig] from HOCON files + environment variable overrides.
 *
 * Precedence (highest first):
 * 1. Environment variables (for secrets and deploy-time overrides)
 * 2. application-{env}.conf (environment overlay)
 * 3. application.conf (defaults)
 *
 * Fails fast on missing required values (better to crash at startup than
 * serve traffic with broken config).
 */
object ConfigLoader {
    fun load(): AppConfig {
        val environment = Environment.from(System.getenv("APP_ENVIRONMENT"))
        val config = loadHocon(environment)

        return AppConfig(
            environment = environment,
            server =
                AppConfig.ServerConfig(
                    host = config.getString("server.host"),
                    port = envInt("PORT") ?: config.getInt("server.port"),
                ),
            database =
                AppConfig.DatabaseConfig(
                    url = envString("DATABASE_URL") ?: config.getString("database.url"),
                    user = envString("DATABASE_USER") ?: config.getString("database.user"),
                    password = envString("DATABASE_PASSWORD") ?: config.getString("database.password"),
                    maxPoolSize = config.getInt("database.maxPoolSize"),
                ),
            llm =
                AppConfig.LlmConfig(
                    geminiApiKey = envString("GEMINI_API_KEY").orEmpty(),
                    anthropicApiKey = envString("ANTHROPIC_API_KEY").orEmpty(),
                    openAiApiKey = envString("OPENAI_API_KEY").orEmpty(),
                ),
        )
    }

    private fun loadHocon(environment: Environment): Config {
        val base = ConfigFactory.parseResources("application.conf")
        val overlayName =
            when (environment) {
                Environment.LOCAL -> "application-local.conf"
                Environment.PRODUCTION -> "application-prod.conf"
            }
        val overlay = ConfigFactory.parseResources(overlayName)
        return overlay.withFallback(base).resolve()
    }

    private fun envString(key: String): String? = System.getenv(key)?.takeIf { it.isNotBlank() }

    private fun envInt(key: String): Int? = System.getenv(key)?.toIntOrNull()
}
