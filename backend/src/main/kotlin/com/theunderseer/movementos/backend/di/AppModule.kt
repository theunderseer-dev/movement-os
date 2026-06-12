package com.theunderseer.movementos.backend.di

import com.theunderseer.movementos.backend.config.AppConfig
import com.theunderseer.movementos.backend.config.ConfigLoader
import org.koin.dsl.module

/**
 * Top-level Koin module for the backend.
 *
 * As Phase 4 progresses, this composes sub-modules (databaseModule, authModule,
 * llmProxyModule). For now it provides config — the root of the dependency graph.
 */
val appModule =
    module {
        single<AppConfig> { ConfigLoader.load() }
    }
