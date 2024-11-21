package com.example.weather.oleksandrBezushko.ui.details

import androidx.compose.runtime.Immutable
import com.example.weather.oleksandrBezushko.data.CurrentWeatherResponse.WindDirection
import com.example.weather.oleksandrBezushko.model.Location
import com.example.weather.oleksandrBezushko.model.Temperature

@Immutable
data class DetailsState(
    val loadState: LoadState = LoadState.Loading,
) {
    sealed interface LoadState {
        data object NoCity : LoadState

        data object Loading : LoadState

        data class Loaded(
            val location: Location,
            val temperature: Temperature,
            val feelsLike: Temperature,
            val conditionIconUrl: String,
            val conditionText: String,
            val windDirection: WindDirection?,
            val uv: Float,
            val humidity: Int,
        ) : LoadState

        data class Error(
            val message: String,
        ) : LoadState
    }
}
