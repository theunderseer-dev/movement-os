package com.theunderseer.movementos.di

import com.theunderseer.movementos.data.DatabaseFactory
import com.theunderseer.movementos.data.local.GoalLocalDataSource
import com.theunderseer.movementos.data.local.ProgramLocalDataSource
import com.theunderseer.movementos.data.local.ProgressLocalDataSource
import com.theunderseer.movementos.data.local.SessionLocalDataSource
import com.theunderseer.movementos.data.local.SyncMetadataLocalDataSource
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
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

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
    }
