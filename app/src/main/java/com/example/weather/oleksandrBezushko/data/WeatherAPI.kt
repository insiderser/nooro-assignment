package com.example.weather.oleksandrBezushko.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPI {

    @GET("current.json")
    suspend fun getCurrentWeather(
        @Query("q") location: String,
    ): CurrentWeatherResponse

    @GET("search.json")
    suspend fun searchLocations(
        @Query("q") query: String,
    ): List<LocationSearchResult>
}

@Serializable
data class CurrentWeatherResponse(
    val current: CurrentWeather,
) {
    @Serializable
    data class CurrentWeather(
        @SerialName("temp_c") val tempC: Float,
        @SerialName("temp_f") val tempF: Float,
        @SerialName("feelslike_c") val feelsLikeC: Float,
        @SerialName("feelslike_f") val feelsLikeF: Float,
        @SerialName("wind_dir") val windDir: WindDirection?,
        val condition: Condition,
        val uv: Float,
        val humidity: Int,
    )

    @Serializable
    data class Condition(
        val text: String,
        val icon: String,
    ) {
        val iconUrl: String
            get() = "https:$icon"
    }

    @Serializable(with = WindDirectionSerializer::class)
    enum class WindDirection {
        N, NNE, NE, ENE, E, ESE, SE, SSE,
        S, SSW, SW, WSW, W, WNW, NW, NNW,
    }
}

@OptIn(ExperimentalSerializationApi::class)
private class WindDirectionSerializer : KSerializer<CurrentWeatherResponse.WindDirection?> {
    override val descriptor = String.serializer().descriptor

    override fun serialize(encoder: Encoder, value: CurrentWeatherResponse.WindDirection?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeString(value.name)
        }
    }

    override fun deserialize(decoder: Decoder): CurrentWeatherResponse.WindDirection? {
        val value = decoder.decodeString()
        return CurrentWeatherResponse.WindDirection.entries.find { it.name == value }
    }
}

@Serializable
data class LocationSearchResult(
    val name: String,
)
