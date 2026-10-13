package com.example.talenttrack.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.talenttrack.screens.FavoritesScreen
import com.example.talenttrack.screens.SearchScreen

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

private const val SEARCH_ROUTE = "search"
private const val FAVORITES_ROUTE = "favorites"

/**
 * [REUTILIZABLE] Esqueleto de navegación Compose y barra inferior.
 * [ADAPTAR] Cambia rutas, pantallas, etiquetas e iconos según el caso; elimina destinos no solicitados.
 */
@Composable
fun AppNavigation(){

    val navController = rememberNavController()
    // [REUTILIZABLE] Contador para pedir a una pantalla que vuelva a consultar datos al seleccionarla.
    var favoritesRefreshKey by remember { mutableIntStateOf(0) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == SEARCH_ROUTE,
                    onClick = {
                        navController.navigate(SEARCH_ROUTE) {
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Find employees"
                        )
                    },
                    label = { Text("Find Employees") }

                )

                NavigationBarItem(
                    selected = currentRoute == FAVORITES_ROUTE,
                    onClick = {
                        favoritesRefreshKey += 1
                        navController.navigate(FAVORITES_ROUTE) {
                            launchSingleTop =  true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.People,
                            contentDescription = "Employees data"
                        )
                    },
                    label = { Text("Employees Data") }
                )
            }
        }
    ) {
        innerPadding ->
        NavHost(
            navController = navController,
            startDestination = SEARCH_ROUTE,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(SEARCH_ROUTE) {
                SearchScreen()
            }

            composable(FAVORITES_ROUTE) {
                FavoritesScreen(refreshKey = favoritesRefreshKey)
            }

        }
    }

}
