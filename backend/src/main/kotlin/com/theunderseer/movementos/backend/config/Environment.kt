package com.theunderseer.movementos.backend.config

/**
 * Deployment environment. Determines which config overlay is loaded.
 */
enum class Environment {
    LOCAL,
    PRODUCTION,
    ;

    companion object {
        fun from(value: String?): Environment = when (value?.lowercase()) {
            "prod", "production" -> PRODUCTION
            else -> LOCAL
        }
    }
}
