package com.example.weather.oleksandrBezushko.ui.search

import android.icu.text.DecimalFormat
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.weather.oleksandrBezushko.model.Location
import com.example.weather.oleksandrBezushko.model.Temperature
import com.example.weather.oleksandrBezushko.model.TemperatureUnit
import com.example.weather.oleksandrBezushko.ui.details.SearchAppBar
import com.example.weather.oleksandrBezushko.ui.theme.Dark
import com.example.weather.oleksandrBezushko.ui.theme.WeatherOleksandrBezushkoTheme

@Composable
fun SearchScreen(
    onNavigateToDetails: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.stateFlow.collectAsState()
    SearchContent(
        state = state,
        onQueryTextChanged = { viewModel.onQueryUpdated(it) },
        onSearchClicked = { viewModel.startSearch() },
        onCitySelected = {
            viewModel.onLocationSelected(it)
            onNavigateToDetails()
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchContent(
    state: SearchState,
    onQueryTextChanged: (String) -> Unit,
    onSearchClicked: () -> Unit,
    onCitySelected: (SearchState.SearchItem) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier.fillMaxSize(),
        topBar = {
            val focusRequester = remember { FocusRequester() }
            SearchAppBar(
                Modifier
                    .statusBarsPadding()
                    .padding(top = 16.dp)
                    .padding(horizontal = 24.dp)
                    .focusRequester(focusRequester),
                query = state.searchQuery,
                onQueryChanged = onQueryTextChanged,
                onSearchClicked = onSearchClicked,
            )
            LaunchedEffect(focusRequester) {
                focusRequester.requestFocus()
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = onSearchClicked,
            modifier = modifier.padding(innerPadding),
        ) {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 24.dp)) {
                items(state.searchResults) { searchItem ->
                    SearchItem(
                        searchItem = searchItem,
                        onCitySelected = onCitySelected,
                    )
                }

                // TODO: What to do in case of an error? Ask the designer.
            }
        }
    }
}

@Composable
private fun SearchItem(
    searchItem: SearchState.SearchItem,
    onCitySelected: (SearchState.SearchItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = searchItem.content

    val locale = Locale.current.platformLocale
    val numbersFormatter = remember(locale) { DecimalFormat.getNumberInstance(locale) }
    val temperatureUnit = remember(locale) { TemperatureUnit.get() }

    Card(
        modifier = modifier
            // TODO: What vertical padding should be used between items? Ask the designer.
            .padding(horizontal = 20.dp, vertical = 8.dp),
        onClick = { onCitySelected(searchItem) },
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    searchItem.location.city,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(top = 16.dp),
                )

                val isLoaded = content is SearchState.SearchItemContent.Loaded
                Row(Modifier.graphicsLayer(alpha = if (isLoaded) 1f else 0f)) {
                    // Example 2 shows degrees to the right. Example 3 shows degrees to the left.
                    // At work, I would actually ask the designer for the correct placement.
                    Spacer(
                        Modifier
                            .padding(top = 12.dp)
                            .size(8.dp)
                            .border(1.5.dp, Dark, CircleShape)
                    )

                    val temperature = if (content is SearchState.SearchItemContent.Loaded) {
                        content.temperature[temperatureUnit]
                    } else {
                        0f
                    }
                    Text(
                        text = numbersFormatter.format(temperature.toDouble()),
                        style = MaterialTheme.typography.displaySmall,
                    )
                }
            }

            when (content) {
                is SearchState.SearchItemContent.Loaded -> {
                    AsyncImage(
                        content.conditionIconUrl,
                        contentDescription = content.conditionText,
                        modifier = Modifier
                            .padding(vertical = 24.dp)
                            .padding(end = 32.dp)
                            .height(67.dp),
                        contentScale = ContentScale.FillHeight,
                        // TODO: add loading placeholder, handle errors...
                    )
                }

                is SearchState.SearchItemContent.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(32.dp)
                            .align(Alignment.CenterVertically),
                    )
                }

                is SearchState.SearchItemContent.Error -> {
                    // TODO: What to do in case of an error? Ask the designer.
                }
            }
        }
    }
}

@Preview
@Composable
fun SearchScreenPreview() {
    WeatherOleksandrBezushkoTheme {
        SearchContent(
            state = SearchState(
                searchQuery = "London",
                isLoading = false,
                error = null,
                searchResults = listOf(
                    SearchState.SearchItem(
                        location = Location("London"),
                        content = SearchState.SearchItemContent.Loaded(
                            temperature = Temperature(20f, 15f),
                            conditionIconUrl = "https://example.com/icon.png",
                            conditionText = "Sunny",
                        ),
                    ),
                    SearchState.SearchItem(
                        location = Location("New York"),
                        content = SearchState.SearchItemContent.Loading,
                    ),
                ),
            ),
            onQueryTextChanged = {},
            onSearchClicked = {},
        )
    }
}
