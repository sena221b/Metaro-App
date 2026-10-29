package com.kampplus.hava.ui

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.city.CityRoute
import com.kampplus.hava.ui.component.FullZuluDashboardScreen
import com.kampplus.hava.ui.forecast.ForecastRoute
import com.kampplus.hava.ui.forecast.ForecastViewModel
import com.kampplus.hava.ui.map.InteractiveWeatherMapScreen

private val CockpitBackground = Color(0xFF071521)

@Composable
fun HavaNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val selectedTab = when {
        currentRoute == AVIATION_ROUTE -> MainTab.METAR
        currentRoute == ZULU_ROUTE -> MainTab.ZULU
        currentRoute == MAP_ROUTE -> MainTab.MAP
        else -> MainTab.WEATHER
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CockpitBackground,
        bottomBar = {
            MetaroBottomBar(selectedTab = selectedTab, onTabSelected = { tab ->
                val route = when (tab) {
                    MainTab.WEATHER -> CITY_ROUTE
                    MainTab.METAR -> AVIATION_ROUTE
                    MainTab.ZULU -> ZULU_ROUTE
                    MainTab.MAP -> MAP_ROUTE
                }
                if (currentRoute != route) {
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            })
        }
    ) { scaffoldPadding ->
        NavHost(
            navController = navController,
            startDestination = CITY_ROUTE,
            modifier = Modifier.padding(scaffoldPadding)
        ) {
            composable(CITY_ROUTE) {
                CityRoute(onCityClick = { city -> navController.navigate(forecastRoute(city)) })
            }
            composable(
                route = FORECAST_ROUTE,
                arguments = listOf(
                    navArgument(ForecastViewModel.ARG_NAME) { type = NavType.StringType; defaultValue = "" },
                    navArgument(ForecastViewModel.ARG_SUBTITLE) { type = NavType.StringType; defaultValue = "" },
                    navArgument(ForecastViewModel.ARG_LATITUDE) { type = NavType.StringType; defaultValue = "0" },
                    navArgument(ForecastViewModel.ARG_LONGITUDE) { type = NavType.StringType; defaultValue = "0" }
                )
            ) {
                ForecastRoute(onBack = { navController.popBackStack() })
            }
            composable(AVIATION_ROUTE) {
                com.kampplus.hava.ui.forecast.AviationRoute()
            }
            composable(ZULU_ROUTE) { ZuluScreen() }
            composable(MAP_ROUTE) { InteractiveWeatherMapScreen() }
        }
    }
}

private const val CITY_ROUTE = "city"
const val AVIATION_ROUTE = "aviation"
private const val ZULU_ROUTE = "zulu"
private const val MAP_ROUTE = "map"
private const val FORECAST_ROUTE = "forecast?name={name}&subtitle={subtitle}&latitude={latitude}&longitude={longitude}"

private fun forecastRoute(city: City): String = buildString {
    append("forecast")
    append("?name=").append(Uri.encode(city.name))
    append("&subtitle=").append(Uri.encode(city.subtitle))
    append("&latitude=").append(city.latitude)
    append("&longitude=").append(city.longitude)
}

@Composable
private fun ZuluScreen() {
    FullZuluDashboardScreen()
}
