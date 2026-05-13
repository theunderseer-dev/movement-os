package com.theunderseer.movementos

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.theunderseer.movementos.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

@HiltAndroidApp
class MovementOSApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MovementOSApplication)
            modules(sharedModule)
        }
    }
}