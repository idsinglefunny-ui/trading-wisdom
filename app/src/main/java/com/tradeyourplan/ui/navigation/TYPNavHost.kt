package com.tradeyourplan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tradeyourplan.ui.alarm.AlarmEditScreen
import com.tradeyourplan.ui.alarm.AlarmListScreen
import com.tradeyourplan.ui.main.MainScreen
import com.tradeyourplan.ui.quote.AddQuoteScreen
import com.tradeyourplan.ui.quote.QuoteListScreen
import com.tradeyourplan.ui.settings.SettingsScreen
import com.tradeyourplan.ui.settings.ThemePickerScreen

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
            MainScreen(
                onNavigateToQuotes = { navController.navigate(Screen.Quotes.route) },
                onNavigateToAlarms = { navController.navigate(Screen.Alarms.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Quotes.route) {
            QuoteListScreen(
                onBack = { navController.popBackStack() },
                onAddQuote = { navController.navigate("add_quote") }
            )
        }

        composable("add_quote") {
            AddQuoteScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Alarms.route) {
            AlarmListScreen(
                onBack = { navController.popBackStack() },
                onAddAlarm = { navController.navigate(Screen.AlarmEdit.create()) },
                onEditAlarm = { alarmId ->
                    navController.navigate(Screen.AlarmEdit.create(alarmId))
                }
            )
        }

        composable(
            route = Screen.AlarmEdit.route,
            arguments = listOf(
                navArgument("alarmId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: 0L
            AlarmEditScreen(
                alarmId = alarmId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onThemePicker = { navController.navigate(Screen.ThemePicker.route) }
            )
        }

        composable(Screen.ThemePicker.route) {
            ThemePickerScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
