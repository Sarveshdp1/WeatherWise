package com.weatherwise.dependency_injection

import com.weatherwise.fragments.home.HomeViewModel
import com.weatherwise.fragments.location.LocationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

@Suppress("DEPRECATION")
val viewModelModule = module {
    viewModel { HomeViewModel(weatherDataRepository = get() ) }
    viewModel { LocationViewModel(weatherDataRepository = get()) }
}