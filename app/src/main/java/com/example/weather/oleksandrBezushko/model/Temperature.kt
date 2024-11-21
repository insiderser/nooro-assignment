package com.example.weather.oleksandrBezushko.model

import androidx.core.text.util.LocalePreferences

data class Temperature(
    val celcius: Float,
    val fahrenheit: Float,
) {
    operator fun get(unit: TemperatureUnit): Float = when (unit) {
        TemperatureUnit.CELSIUS -> celcius
        TemperatureUnit.FAHRENHEIT -> fahrenheit
    }
}

enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT,
    ;

    companion object {
        fun get(): TemperatureUnit {
            val unit = LocalePreferences.getTemperatureUnit()
            if (unit == LocalePreferences.TemperatureUnit.FAHRENHEIT) {
                return FAHRENHEIT
            } else {
                return CELSIUS
            }
        }
    }
}
