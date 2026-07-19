package com.weatherwise.dependency_injection

import com.weatherwise.network.repository.WeatherDataRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { WeatherDataRepository() }
}