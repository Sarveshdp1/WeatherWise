package com.weatherwise.utils

import android.app.Application
import com.weatherwise.dependency_injection.repositoryModule
import com.weatherwise.dependency_injection.viewModelModule
import org.koin.core.context.startKoin

class AppConfig : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(listOf(repositoryModule, viewModelModule))
        }
    }
}