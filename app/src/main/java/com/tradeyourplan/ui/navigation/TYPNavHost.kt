package com.tradeyourplan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tradeyourplan.ui.main.MainScreen

sealed class Screen(val route: String) {
    object Main : Screen("main")
}

@Composable
fun TYPNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Main.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Main.route) {
            MainScreen()
        }
    }
}
