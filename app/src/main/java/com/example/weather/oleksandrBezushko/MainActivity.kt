package com.example.weather.oleksandrBezushko

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weather.oleksandrBezushko.ui.Screens
import com.example.weather.oleksandrBezushko.ui.details.DetailsScreen
import com.example.weather.oleksandrBezushko.ui.search.SearchScreen
import com.example.weather.oleksandrBezushko.ui.theme.WeatherOleksandrBezushkoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherOleksandrBezushkoTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Screens.Details,
                ) {
                    composable<Screens.Details> {
                        DetailsScreen(
                            onNavigateToSearch = {
                                navController.navigate(Screens.Search)
                            }
                        )
                    }

                    composable<Screens.Search> {
                        SearchScreen(
                            onNavigateToDetails = {
                                navController.popBackStack()
                                navController.navigate(Screens.Details)
                            }
                        )
                    }
                }
            }
        }
    }
}
