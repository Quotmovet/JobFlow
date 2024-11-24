package ru.practicum.android.diploma.di

import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module
import ru.practicum.android.diploma.presentation.details.DetailsViewModel
import ru.practicum.android.diploma.presentation.favorite.FavoriteViewModel
import ru.practicum.android.diploma.presentation.search.SearchViewModel
import ru.practicum.android.diploma.presentation.filter.FilterViewModel
import ru.practicum.android.diploma.presentation.filter.industry.IndustryViewModel

val viewModelModule = module {
    viewModelOf(::SearchViewModel)
    viewModelOf(::FavoriteViewModel)
    viewModelOf(::DetailsViewModel)
    viewModelOf(::IndustryViewModel)
    viewModelOf(::FilterViewModel)
}
