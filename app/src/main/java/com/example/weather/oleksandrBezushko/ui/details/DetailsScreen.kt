package com.example.weather.oleksandrBezushko.ui.details

import android.icu.text.DecimalFormat
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.weather.oleksandrBezushko.R
import com.example.weather.oleksandrBezushko.data.CurrentWeatherResponse
import com.example.weather.oleksandrBezushko.model.Location
import com.example.weather.oleksandrBezushko.model.Temperature
import com.example.weather.oleksandrBezushko.model.TemperatureUnit
import com.example.weather.oleksandrBezushko.ui.theme.Dark
import com.example.weather.oleksandrBezushko.ui.theme.Gray
import com.example.weather.oleksandrBezushko.ui.theme.WeatherOleksandrBezushkoTheme

@Composable
fun DetailsScreen(
    onNavigateToSearch: () -> Unit,
    viewModel: DetailsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.stateFlow.collectAsState()
    DetailsContent(
        state = state,
        onNavigateToSearch = onNavigateToSearch,
        onTryAgainClicked = { viewModel.refresh() },
        modifier = modifier,
    )
}

@Composable
private fun DetailsContent(
    state: DetailsState,
    onNavigateToSearch: () -> Unit,
    onTryAgainClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier.fillMaxSize(),
        topBar = {
            SearchAppBar(
                Modifier
                    .statusBarsPadding()
                    .padding(top = 16.dp)
                    .padding(horizontal = 24.dp)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        onNavigateToSearch()
                    },
                enabled = false,
            )
        }
    ) { innerPadding ->
        when (state.loadState) {
            is DetailsState.LoadState.Error -> {
                ErrorContent(
                    modifier = modifier.padding(innerPadding),
                    error = state.loadState,
                    onTryAgainClicked = onTryAgainClicked,
                )
            }

            is DetailsState.LoadState.Loaded -> {
                LoadedContent(
                    modifier = modifier.padding(innerPadding),
                    state = state.loadState,
                )
            }

            is DetailsState.LoadState.Loading -> {
                LoadingContent(modifier = modifier.padding(innerPadding))
            }

            is DetailsState.LoadState.NoCity -> {
                NoCityContent(modifier = modifier.padding(innerPadding))
            }
        }
    }
}

@Composable
fun SearchAppBar(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    query: String = "",
    onQueryChanged: (String) -> Unit = {},
    onSearchClicked: () -> Unit = {},
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = {
            Text(
                "Search Location", // UI strings should be extracted to allow translations
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingIcon = {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,

            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,

            unfocusedTextColor = Dark,
            focusedTextColor = Dark,
            disabledTextColor = Dark,
        ),
        shape = RoundedCornerShape(16.dp),
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            autoCorrectEnabled = true,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearchClicked() },
        ),
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun NoCityContent(modifier: Modifier) {
    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.aligned(BiasAlignment.Vertical(-0.2f)),
    ) {
        // UI strings should be extracted to allow translations
        Text("No City Selected", style = MaterialTheme.typography.headlineLarge)
        Text("Please Search For A City", style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
private fun LoadingContent(modifier: Modifier) {
    // No design provided for loading state. Just showing a loader.
    // At work, I would ask the designer for a design.
    Box(modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = modifier.align(BiasAlignment(0f, -0.3f)))
    }
}

@Composable
private fun ErrorContent(
    modifier: Modifier,
    error: DetailsState.LoadState.Error,
    onTryAgainClicked: () -> Unit,
) {
    // No design provided for error state. Just showing a text.
    // At work, I would ask the designer for a design.
    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.aligned(BiasAlignment.Vertical(-0.2f)),
    ) {
        // UI strings should be extracted to allow translations
        Text("Error", style = MaterialTheme.typography.headlineLarge)
        Text(error.message, style = MaterialTheme.typography.headlineSmall)
        Button(onClick = onTryAgainClicked) {
            Text("Try Again")
        }
    }
}

@Composable
private fun LoadedContent(
    modifier: Modifier,
    state: DetailsState.LoadState.Loaded,
) {
    val locale = Locale.current.platformLocale
    val numbersFormatter = remember(locale) { DecimalFormat.getNumberInstance(locale) }
    val temperatureUnit = remember(locale) { TemperatureUnit.get() }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.aligned(BiasAlignment.Vertical(-0.5f)),
    ) {
        AsyncImage(
            model = state.conditionIconUrl,
            contentDescription = state.conditionText,
            Modifier.size(123.dp),
            contentScale = ContentScale.Fit,
            // TODO: add loading placeholder, handle errors...
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                state.location.city,
                style = MaterialTheme.typography.headlineLarge,
            )

            if (state.windDirection != null) {
                Icon(
                    painterResource(R.drawable.ic_wind_direction),
                    contentDescription = "Wind direction: ${state.windDirection}", // TODO: Add good description for accessibility
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .rotate(state.windDirection.rotationDegrees),
                )
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top,
        ) {
            // Example 2 shows degrees to the right. Example 3 shows degrees to the left.
            // At work, I would actually ask the designer for the correct placement.
            Spacer(
                Modifier
                    .padding(top = 12.dp)
                    .size(8.dp)
                    .border(1.5.dp, Dark, CircleShape)
            )

            Text(
                text = numbersFormatter.format(state.temperature[temperatureUnit].toDouble()),
                style = MaterialTheme.typography.displayMedium,
            )
        }

        Card(
            modifier = Modifier
                .padding(top = 36.dp)
                .width(274.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                @Composable
                fun WeatherDetail(label: String, value: String) {
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            value,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = Gray,
                        )
                    }
                }

                WeatherDetail("Humidity", "${state.humidity}%")
                WeatherDetail("UV", numbersFormatter.format(state.uv))
                WeatherDetail("Feels Like", "${numbersFormatter.format(state.feelsLike[temperatureUnit].toDouble())}°")
            }
        }
    }
}

private val CurrentWeatherResponse.WindDirection.rotationDegrees: Float
    get() = when (this) {
        CurrentWeatherResponse.WindDirection.N -> -45f
        CurrentWeatherResponse.WindDirection.NNE -> -22.5f
        CurrentWeatherResponse.WindDirection.NE -> 0f
        CurrentWeatherResponse.WindDirection.ENE -> 22.5f
        CurrentWeatherResponse.WindDirection.E -> 45f
        CurrentWeatherResponse.WindDirection.ESE -> 67.5f
        CurrentWeatherResponse.WindDirection.SE -> 90f
        CurrentWeatherResponse.WindDirection.SSE -> 112.5f
        CurrentWeatherResponse.WindDirection.S -> 135f
        CurrentWeatherResponse.WindDirection.SSW -> 157.5f
        CurrentWeatherResponse.WindDirection.SW -> 180f
        CurrentWeatherResponse.WindDirection.WSW -> 202.5f
        CurrentWeatherResponse.WindDirection.W -> 225f
        CurrentWeatherResponse.WindDirection.WNW -> 247.5f
        CurrentWeatherResponse.WindDirection.NW -> 270f
        CurrentWeatherResponse.WindDirection.NNW -> 292.5f
    }

@Preview
@Composable
fun PreviewDetailsScreenNoCity() {
    WeatherOleksandrBezushkoTheme {
        DetailsContent(
            state = DetailsState(loadState = DetailsState.LoadState.NoCity),
            onNavigateToSearch = {},
            onTryAgainClicked = {},
        )
    }
}

@Preview
@Composable
fun PreviewDetailsScreenLoading() {
    WeatherOleksandrBezushkoTheme {
        DetailsContent(
            state = DetailsState(loadState = DetailsState.LoadState.Loading),
            onNavigateToSearch = {},
            onTryAgainClicked = {},
        )
    }
}

@Preview
@Composable
fun PreviewDetailsScreenError() {
    WeatherOleksandrBezushkoTheme {
        DetailsContent(
            state = DetailsState(loadState = DetailsState.LoadState.Error("Error message")),
            onNavigateToSearch = {},
            onTryAgainClicked = {},
        )
    }
}

@Preview
@Composable
fun PreviewDetailsScreenLoaded() {
    WeatherOleksandrBezushkoTheme {
        DetailsContent(
            state = DetailsState(
                DetailsState.LoadState.Loaded(
                    location = Location("City"),
                    temperature = Temperature(20f, 68f),
                    feelsLike = Temperature(22f, 71.6f),
                    conditionIconUrl = "https://example.com/icon.png",
                    conditionText = "Sunny",
                    windDirection = CurrentWeatherResponse.WindDirection.N,
                    uv = 5f,
                    humidity = 50,
                )
            ),
            onNavigateToSearch = {},
            onTryAgainClicked = {},
        )
    }
}
