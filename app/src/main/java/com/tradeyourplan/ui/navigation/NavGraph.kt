// app/src/main/java/com/tradeyourplan/ui/navigation/NavGraph.kt
package com.tradeyourplan.ui.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Quotes : Screen("quotes")
    object AlarmEdit : Screen("alarm_edit/{alarmId}") {
        fun create(alarmId: Long = 0) = "alarm_edit/$alarmId"
    }
    object Settings : Screen("settings")
    object ThemePicker : Screen("theme_picker")
}
