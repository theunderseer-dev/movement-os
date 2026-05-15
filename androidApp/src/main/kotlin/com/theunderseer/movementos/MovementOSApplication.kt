package com.theunderseer.movementos

import android.app.Application
import com.theunderseer.movementos.data.DatabaseFactory
import com.theunderseer.movementos.di.sharedModule
import dagger.hilt.android.HiltAndroidApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

@HiltAndroidApp
class MovementOSApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MovementOSApplication)
            modules(
                platformModule(this@MovementOSApplication),
                sharedModule,
            )
        }
    }
}

private fun platformModule(application: Application) =
    module {
        single { DatabaseFactory(application.applicationContext) }
    }
