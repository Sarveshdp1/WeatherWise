package com.weatherwise.dependency_injection

import com.weatherwise.fragments.home.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@Suppress("DEPRECATION")
val viewModelModule = module {
    viewModel { HomeViewModel(weatherDataRepository = get() ) }
}