package com.example.weather.oleksandrBezushko.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.oleksandrBezushko.data.AppStorage
import com.example.weather.oleksandrBezushko.data.WeatherAPI
import com.example.weather.oleksandrBezushko.model.Location
import com.example.weather.oleksandrBezushko.model.Temperature
import com.example.weather.oleksandrBezushko.model.TemperatureUnit
import com.example.weather.oleksandrBezushko.utils.ObserveIsNetworkAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val storage: AppStorage,
    private val weatherAPI: WeatherAPI,
    observeIsNetworkAvailable: ObserveIsNetworkAvailable,
) : ViewModel() {

    private val temperatureUnit = storage.temperatureUnitFlow
        .map { it ?: TemperatureUnit.get() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _stateFlow = MutableStateFlow(DetailsState())
    val stateFlow: StateFlow<DetailsState> = _stateFlow

    init {
        refresh()

        observeIsNetworkAvailable().drop(1).onEach { isNetworkAvailable ->
            if (isNetworkAvailable && stateFlow.value.loadState is DetailsState.LoadState.Error) {
                refresh()
            }
        }.launchIn(viewModelScope)

        temperatureUnit.onEach { unit ->
            if (unit != null) {
                _stateFlow.update { state ->
                    if (state.loadState is DetailsState.LoadState.Loaded) {
                        state.copy(
                            loadState = state.loadState.copy(
                                temperatureUnit = unit,
                            )
                        )
                    } else {
                        state
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun refresh() {
        viewModelScope.launch {
            _stateFlow.update { it.copy(loadState = DetailsState.LoadState.Loading) }
            val savedCity = storage.getSavedCity()
            if (savedCity.isNullOrEmpty()) {
                _stateFlow.update { it.copy(loadState = DetailsState.LoadState.NoCity) }
                return@launch
            }

            val response = runCatching {
                weatherAPI.getCurrentWeather(location = savedCity)
            }.getOrElse { e ->
                // Need proper logging. This is just for the assignment
                e.printStackTrace()
                _stateFlow.update {
                    // Need proper error messages. Probably a good idea to automatically retry.
                    // But need to be careful with 5** errors.
                    it.copy(loadState = DetailsState.LoadState.Error(e.message ?: e.javaClass.simpleName))
                }
                return@launch
            }
            val temperatureUnit = temperatureUnit.first { it != null }!!
            val cityData = DetailsState.LoadState.Loaded(
                location = Location(savedCity),
                temperature = Temperature(
                    celcius = response.current.tempC,
                    fahrenheit = response.current.tempF,
                ),
                feelsLike = Temperature(
                    celcius = response.current.feelsLikeC,
                    fahrenheit = response.current.feelsLikeF,
                ),
                conditionIconUrl = response.current.condition.iconUrl,
                conditionText = response.current.condition.text,
                windDirection = response.current.windDir,
                uv = response.current.uv,
                humidity = response.current.humidity,
                temperatureUnit = temperatureUnit,
            )
            _stateFlow.update { it.copy(loadState = cityData) }
        }
    }

    fun onTemperatureUnitChanged(unit: TemperatureUnit) {
        viewModelScope.launch {
            storage.saveTemperatureUnit(unit)
        }
    }
}
