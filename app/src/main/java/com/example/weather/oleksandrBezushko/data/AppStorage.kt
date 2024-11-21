package com.example.weather.oleksandrBezushko.data

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface AppStorage {

    suspend fun getSavedCity(): String?
    suspend fun saveCity(city: String)
}

private const val FILE_NAME = "app_prefs"
private const val SAVED_CITY_KEY = "city"

@Singleton
class SharedPrefsAppStorage @Inject constructor(
    @ApplicationContext context: Context,
) : AppStorage {

    // Better to have dispatchers as a parameter in the constructor and managed by the DI
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO

    private val prefs by lazy {
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    }

    override suspend fun getSavedCity(): String? = withContext(dispatcher) {
        prefs.getString(SAVED_CITY_KEY, null)
    }

    override suspend fun saveCity(city: String) = withContext(dispatcher) {
        prefs.edit(true) {
            putString(SAVED_CITY_KEY, city)
        }
    }
}