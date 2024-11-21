package com.example.weather.oleksandrBezushko.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// It's a good idea to have a defined color system in apps.
// This needs to be discussed with design.

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val Dark = Color(0xFF2C2C2C)
val Gray = Color(0xFF9A9A9A)

val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color(0xFFF2F2F2),

    // Very low contrast between surfaceVariant and onSurfaceVariant.
    // Would discuss with design to see. Not good for accessibility.
    onSurfaceVariant = Color(0xFFC4C4C4),
)
