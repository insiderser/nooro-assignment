package com.example.weather.oleksandrBezushko.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.weather.oleksandrBezushko.model.TemperatureUnit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface AppStorage {

    suspend fun getSavedCity(): String?
    suspend fun saveCity(city: String)

    suspend fun getTemperatureUnit(): TemperatureUnit?
    suspend fun saveTemperatureUnit(unit: TemperatureUnit)
    val temperatureUnitFlow: Flow<TemperatureUnit?>
}

private const val FILE_NAME = "app_prefs"
private const val SAVED_CITY_KEY = "city"
private const val TEMPERATURE_UNIT_KEY = "temperature_unit"

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

    override suspend fun getTemperatureUnit(): TemperatureUnit? = withContext(dispatcher) {
        val unit = prefs.getString(TEMPERATURE_UNIT_KEY, null)
        if (unit == TemperatureUnit.FAHRENHEIT.name) {
            TemperatureUnit.FAHRENHEIT
        } else {
            TemperatureUnit.CELSIUS
        }
    }

    override suspend fun saveTemperatureUnit(unit: TemperatureUnit) = withContext(dispatcher) {
        prefs.edit(true) {
            putString(TEMPERATURE_UNIT_KEY, unit.name)
        }
    }

    override val temperatureUnitFlow: Flow<TemperatureUnit?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == TEMPERATURE_UNIT_KEY) {
                launch {
                    send(getTemperatureUnit())
                }
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)

        send(getTemperatureUnit())

        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
}