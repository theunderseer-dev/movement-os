package com.theunderseer.movementos.di

import com.theunderseer.movementos.data.DatabaseFactory
import com.theunderseer.movementos.data.ai.LlmApiKeys
import com.theunderseer.movementos.data.ai.LlmClient
import com.theunderseer.movementos.data.ai.LlmProvider
import com.theunderseer.movementos.data.ai.adapter.LlmAdapter
import com.theunderseer.movementos.data.ai.adapter.anthropic.AnthropicLlmAdapter
import com.theunderseer.movementos.data.ai.adapter.gemini.GeminiLlmAdapter
import com.theunderseer.movementos.data.ai.adapter.openai.OpenAiLlmAdapter
import com.theunderseer.movementos.data.ai.orchestrator.DefaultLlmRequestStrategy
import com.theunderseer.movementos.data.ai.orchestrator.LlmOrchestrator
import com.theunderseer.movementos.data.ai.orchestrator.LlmRequestStrategy
import com.theunderseer.movementos.data.ai.orchestrator.ProviderHealthTracker
import com.theunderseer.movementos.data.ai.prompt.PromptBuilder
import com.theunderseer.movementos.data.ai.prompt.ResponseParser
import com.theunderseer.movementos.data.ai.prompt.program.DeterministicProgramGenerator
import com.theunderseer.movementos.data.ai.prompt.program.ProgramGenerator
import com.theunderseer.movementos.data.ai.prompt.retry.ResponseRepairStrategy
import com.theunderseer.movementos.data.local.GoalLocalDataSource
import com.theunderseer.movementos.data.local.ProgramLocalDataSource
import com.theunderseer.movementos.data.local.ProgressLocalDataSource
import com.theunderseer.movementos.data.local.SessionLocalDataSource
import com.theunderseer.movementos.data.local.SyncMetadataLocalDataSource
import com.theunderseer.movementos.data.network.NetworkConfig
import com.theunderseer.movementos.data.network.auth.AuthRefreshHandler
import com.theunderseer.movementos.data.network.auth.AuthTokenStorage
import com.theunderseer.movementos.data.network.auth.InMemoryAuthTokenStorage
import com.theunderseer.movementos.data.network.auth.StubAuthRefreshHandler
import com.theunderseer.movementos.data.network.createHttpClient
import com.theunderseer.movementos.data.network.llm.LlmApiClient
import com.theunderseer.movementos.data.network.llm.LlmApiClientKtorImpl
import com.theunderseer.movementos.data.network.remote.GoalRemoteDataSourceKtorImpl
import com.theunderseer.movementos.data.network.remote.ProgramRemoteDataSourceKtorImpl
import com.theunderseer.movementos.data.network.remote.SessionRemoteDataSourceKtorImpl
import com.theunderseer.movementos.data.orchestration.RepositoryOrchestration
import com.theunderseer.movementos.data.orchestration.StaleChecker
import com.theunderseer.movementos.data.remote.GoalRemoteDataSource
import com.theunderseer.movementos.data.remote.ProgramRemoteDataSource
import com.theunderseer.movementos.data.remote.SessionRemoteDataSource
import com.theunderseer.movementos.data.remote.StubGoalRemoteDataSource
import com.theunderseer.movementos.data.remote.StubProgramRemoteDataSource
import com.theunderseer.movementos.data.remote.StubSessionRemoteDataSource
import com.theunderseer.movementos.data.repository.DefaultGoalRepository
import com.theunderseer.movementos.data.repository.DefaultProgramRepository
import com.theunderseer.movementos.data.repository.DefaultSessionRepository
import com.theunderseer.movementos.domain.repository.GoalRepository
import com.theunderseer.movementos.domain.repository.ProgramRepository
import com.theunderseer.movementos.domain.repository.SessionRepository
import com.theunderseer.movementos.domain.usecase.GenerateProgramUseCase
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import com.theunderseer.movementos.domain.usecase.ProgramGenerator as IProgramGenerator

@OptIn(ExperimentalTime::class)
val sharedModule =
    module {
        single { SyncMetadataLocalDataSource(get(), get(named("io"))) }

        single<ProgramRemoteDataSource> { StubProgramRemoteDataSource() }
        single<SessionRemoteDataSource> { StubSessionRemoteDataSource() }
        single<GoalRemoteDataSource> { StubGoalRemoteDataSource() }

        single(named("programStaleChecker")) { StaleChecker(ttl = 1.hours) }
        single(named("sessionStaleChecker")) { StaleChecker(ttl = 24.hours) }
        single(named("goalStaleChecker")) { StaleChecker(ttl = 6.hours) }

        single { get<DatabaseFactory>().create() }
        single(named("io")) { Dispatchers.Default }

        single { SessionLocalDataSource(get(), get(named("io"))) }
        single { ProgramLocalDataSource(get(), get(), get(named("io"))) }
        single { GoalLocalDataSource(get(), get(named("io"))) }
        single { ProgressLocalDataSource(get(), get(named("io"))) }

        single {
            RepositoryOrchestration(
                syncMetadata = get(),
                staleChecker = get(named("programStaleChecker")),
                dispatcher = get(named("io")),
            )
        }

        single {
            NetworkConfig(
                baseUrl = "https://api.movementos.dev", // placeholder; replace in Phase 4
                isDebug = true, // platform-specific via expect/actual later
            )
        }
        single<AuthTokenStorage> { InMemoryAuthTokenStorage() }
        single<AuthRefreshHandler> { StubAuthRefreshHandler() }
        single { createHttpClient(get(), get(), get()) }

        single<LlmApiClient> { LlmApiClientKtorImpl(get()) }

        single<ProgramRemoteDataSource> { ProgramRemoteDataSourceKtorImpl(get()) }
        single<SessionRemoteDataSource> { SessionRemoteDataSourceKtorImpl(get()) }
        single<GoalRemoteDataSource> { GoalRemoteDataSourceKtorImpl(get()) }

        single<ProgramRepository> {
            DefaultProgramRepository(
                local = get(),
                remote = get(),
                orchestration = get(),
            )
        }

        single<SessionRepository> {
            DefaultSessionRepository(
                sessionDataSource = get(),
                progressDataSource = get(),
                remote = get(),
                orchestration = get(),
            )
        }
        single<GoalRepository> {
            DefaultGoalRepository(
                local = get(),
                remote = get(),
                orchestration = get(),
            )
        }

        single<ProviderHealthTracker> { ProviderHealthTracker() }
        single<LlmRequestStrategy> { DefaultLlmRequestStrategy() }

        single<LlmAdapter>(named("gemini")) {
            GeminiLlmAdapter(httpClient = get(), apiKey = get<LlmApiKeys>().geminiApiKey)
        }
        single<LlmAdapter>(named("anthropic")) {
            AnthropicLlmAdapter(httpClient = get(), apiKey = get<LlmApiKeys>().anthropicApiKey)
        }
        single<LlmAdapter>(named("openai")) {
            OpenAiLlmAdapter(httpClient = get(), apiKey = get<LlmApiKeys>().openAiApiKey)
        }

        single<LlmClient> {
            LlmOrchestrator(
                adapters =
                    mapOf(
                        LlmProvider.GEMINI to get(named("gemini")),
                        LlmProvider.ANTHROPIC to get(named("anthropic")),
                        LlmProvider.OPENAI to get(named("openai")),
                    ),
                strategy = get(),
                healthTracker = get(),
            )
        }

        single { PromptBuilder() }
        single { ResponseParser() }
        single { ResponseRepairStrategy(llmClient = get()) }

        single<IProgramGenerator>(named("llm")) {
            ProgramGenerator(
                llmClient = get(),
                promptBuilder = get(),
                responseParser = get(),
            )
        }

        single<IProgramGenerator>(named("fallback")) {
            DeterministicProgramGenerator()
        }

        single<ProgramGenerator> { get(named("llm")) }

        single {
            GenerateProgramUseCase(
                programRepository = get(),
                programGenerator = get(),
                fallbackGenerator = get(named("fallback")),
            )
        }
    }
