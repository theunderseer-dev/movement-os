package com.theunderseer.movementos.di

import com.theunderseer.movementos.data.DatabaseFactory
import com.theunderseer.movementos.data.local.GoalLocalDataSource
import com.theunderseer.movementos.data.local.ProgramLocalDataSource
import com.theunderseer.movementos.data.local.ProgressLocalDataSource
import com.theunderseer.movementos.data.local.SessionLocalDataSource
import com.theunderseer.movementos.data.repository.DefaultGoalRepository
import com.theunderseer.movementos.data.repository.DefaultProgramRepository
import com.theunderseer.movementos.data.repository.DefaultSessionRepository
import com.theunderseer.movementos.domain.repository.GoalRepository
import com.theunderseer.movementos.domain.repository.ProgramRepository
import com.theunderseer.movementos.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
val sharedModule =
    module {
        single { get<DatabaseFactory>().create() }
        single(named("io")) { Dispatchers.Default }

        single { SessionLocalDataSource(get(), get(named("io"))) }
        single { ProgramLocalDataSource(get(), get(), get(named("io"))) }
        single { GoalLocalDataSource(get(), get(named("io"))) }
        single { ProgressLocalDataSource(get(), get(named("io"))) }

        single<ProgramRepository> { DefaultProgramRepository(get()) }
        single<SessionRepository> { DefaultSessionRepository(get(), get()) }
        single<GoalRepository> { DefaultGoalRepository(get()) }
    }
