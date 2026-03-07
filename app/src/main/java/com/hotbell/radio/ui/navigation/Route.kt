package com.hotbell.radio.ui.navigation

sealed class Route(val route: String) {
    data object Home : Route("home")
    data object RadioExplorer : Route("radio_explorer/{mode}") {
        fun create(mode: String = "general") = "radio_explorer/$mode"
    }
    data object Favorites : Route("favorites")
    data object Settings : Route("settings")
    data object AlarmEdit : Route("alarm_edit?alarmId={alarmId}") {
        fun create(alarmId: String? = null) =
            if (alarmId != null) "alarm_edit?alarmId=$alarmId" else "alarm_edit"
    }
}
