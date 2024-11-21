package com.example.weather.oleksandrBezushko.ui.search

import androidx.compose.runtime.Immutable
import com.example.weather.oleksandrBezushko.model.Location
import com.example.weather.oleksandrBezushko.model.Temperature

@Immutable
data class SearchState(
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchResults: List<SearchItem> = emptyList(),
) {

    data class SearchItem(
        val location: Location,
        val content: SearchItemContent,
    )

    sealed interface SearchItemContent {
        data class Loaded(
            val temperature: Temperature,
            val conditionIconUrl: String,
            val conditionText: String,
        ) : SearchItemContent

        data object Loading : SearchItemContent

        data class Error(
            val message: String,
        ) : SearchItemContent
    }
}
