package com.example.weather.oleksandrBezushko.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.oleksandrBezushko.data.AppStorage
import com.example.weather.oleksandrBezushko.data.CurrentWeatherResponse
import com.example.weather.oleksandrBezushko.data.WeatherAPI
import com.example.weather.oleksandrBezushko.model.Location
import com.example.weather.oleksandrBezushko.model.Temperature
import com.example.weather.oleksandrBezushko.utils.ObserveIsNetworkAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import javax.inject.Inject

private const val SEARCH_QUERY_KEY = "searchQuery"

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val weatherAPI: WeatherAPI,
    observeIsNetworkAvailable: ObserveIsNetworkAvailable,
    private val storage: AppStorage,
) : ViewModel() {

    private val _stateFlow = MutableStateFlow(
        SearchState(
            searchQuery = savedStateHandle[SEARCH_QUERY_KEY] ?: "",
        )
    )
    val stateFlow: StateFlow<SearchState> = _stateFlow

    private var searchJob: Job? = null

    private val currentWeatherCache = mutableMapOf<Location, SearchState.SearchItemContent.Loaded>()
    private val loadItemContentJobs = mutableMapOf<Location, Job>()

    // Limit the number of concurrent requests to 3
    private val loadItemsSemaphore = Semaphore(3)

    init {
        observeIsNetworkAvailable().drop(1).onEach { isNetworkAvailable ->
            if (isNetworkAvailable && !stateFlow.value.error.isNullOrEmpty()) {
                startSearch()
            }
        }.launchIn(viewModelScope)

        stateFlow.onEach { refreshLoadingItemDetails() }.launchIn(viewModelScope)
    }

    fun onQueryUpdated(query: String) {
        _stateFlow.update { it.copy(searchQuery = query) }
        savedStateHandle[SEARCH_QUERY_KEY] = query
    }

    fun startSearch() {
        // TODO: don't cancel if already loading the same query.
        //  Or if already loaded, don't reload.
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val query = stateFlow.value.searchQuery.trim()
            if (query.isEmpty()) {
                _stateFlow.update { it.copy(isLoading = false, error = null) }
                return@launch
            }
            _stateFlow.update { it.copy(isLoading = true, error = null) }

            val response = runCatching {
                weatherAPI.searchLocations(query)
            }.getOrElse { e ->
                // Need proper logging. This is just for the assignment
                e.printStackTrace()
                _stateFlow.update {
                    // Need proper error messages. Probably a good idea to automatically retry.
                    // But need to be careful with 5** errors.
                    it.copy(error = e.message ?: e.javaClass.simpleName, isLoading = false)
                }
                return@launch
            }
            val searchResults = response.map { responseItem ->
                val location = Location(responseItem.name)
                val cachedData = currentWeatherCache[location]
                SearchState.SearchItem(
                    location = location,
                    content = cachedData ?: SearchState.SearchItemContent.Loading,
                )
            }
            _stateFlow.update { it.copy(isLoading = false, searchResults = searchResults) }
        }
    }

    private fun refreshLoadingItemDetails() {
        stateFlow.value.searchResults.forEach { searchItem ->
            if (searchItem.content !is SearchState.SearchItemContent.Loaded &&
                searchItem.location !in loadItemContentJobs
            ) {
                loadItemContent(searchItem.location)
            }
        }

        val currentLocations = stateFlow.value.searchResults.map { it.location }.toSet()
        loadItemContentJobs.keys.forEach { location ->
            if (location !in currentLocations) {
                loadItemContentJobs[location]?.cancel()
                loadItemContentJobs.remove(location)
            }
        }
    }

    private fun loadItemContent(location: Location) {
        if (loadItemContentJobs.containsKey(location)) return
        loadItemContentJobs[location] = viewModelScope.launch {
            updateItemContent(location, SearchState.SearchItemContent.Loading)
            val response = executeItemContentRequest(location).getOrElse { e ->
                // Need proper logging. This is just for the assignment
                e.printStackTrace()
                updateItemContent(location, SearchState.SearchItemContent.Error(e.message ?: e.javaClass.simpleName))
                // Can retry here. Or observe network state and retry when network is available.
                return@launch
            }
            val content = SearchState.SearchItemContent.Loaded(
                temperature = Temperature(
                    celcius = response.current.tempC,
                    fahrenheit = response.current.tempF,
                ),
                conditionIconUrl = response.current.condition.iconUrl,
                conditionText = response.current.condition.text,
            )
            currentWeatherCache[location] = content
            updateItemContent(location, content)
            loadItemContentJobs.remove(location)
        }
    }

    private suspend fun executeItemContentRequest(location: Location): Result<CurrentWeatherResponse> =
        loadItemsSemaphore.withPermit {
            runCatching {
                weatherAPI.getCurrentWeather(location.city)
            }
        }

    private fun updateItemContent(location: Location, newContent: SearchState.SearchItemContent) {
        _stateFlow.update {
            it.copy(searchResults = it.searchResults.map { searchItem ->
                if (searchItem.location == location) {
                    searchItem.copy(content = newContent)
                } else {
                    searchItem
                }
            })
        }
    }

    fun onLocationSelected(item: SearchState.SearchItem) {
        viewModelScope.launch {
            storage.saveCity(item.location.city)
        }
    }
}
