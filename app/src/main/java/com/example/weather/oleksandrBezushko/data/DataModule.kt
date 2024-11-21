package com.example.weather.oleksandrBezushko.data

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import javax.inject.Singleton

private const val BASE_URL = "https://api.weatherapi.com/v1/"

// TODO: app credentials should not live in the app code
private const val API_KEY = "0f8f6f6662284a10952205718242011"

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    fun bindAppStorage(impl: SharedPrefsAppStorage): AppStorage

    companion object {

        @Provides
        @Singleton
        fun provideWeatherAPI(): WeatherAPI {
            val okHttp = OkHttpClient.Builder()
                .addNetworkInterceptor(HttpLoggingInterceptor().apply {
                    // Make sure no logging in production, especially credentials
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .url(chain.request().url.newBuilder().addQueryParameter("key", API_KEY).build())
                        .build()
                    chain.proceed(request)
                }
                .build()
            val json = Json {
                this.ignoreUnknownKeys = true
                this.isLenient = true
                this.encodeDefaults = true
            }
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttp)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
            return retrofit.create()
        }
    }
}