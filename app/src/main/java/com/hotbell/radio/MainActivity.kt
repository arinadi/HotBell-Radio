package com.hotbell.radio

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import com.hotbell.radio.ui.alarm.AlarmEditScreen
import com.hotbell.radio.ui.alarm.AlarmEditViewModel
import com.hotbell.radio.ui.home.HomeScreen
import com.hotbell.radio.ui.home.HomeViewModel
import com.hotbell.radio.ui.navigation.Route
import com.hotbell.radio.ui.radio.RadioExplorerScreen
import com.hotbell.radio.ui.radio.RadioViewModel
import com.hotbell.radio.ui.theme.HotBellTheme
import com.hotbell.radio.ui.theme.PitchBlack
import android.app.AlarmManager
import android.content.Context

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkExactAlarmPermission()

        setContent {
            HotBellTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PitchBlack
                ) {
                    val navController = rememberNavController()
                    val radioViewModel: RadioViewModel = viewModel()

                    NavHost(
                        navController = navController,
                        startDestination = Route.Home.route
                    ) {
                        // Home Screen
                        composable(Route.Home.route) {
                            val homeViewModel: HomeViewModel = viewModel()
                            HomeScreen(
                                viewModel = homeViewModel,
                                onAddAlarm = {
                                    navController.navigate(Route.AlarmEdit.create())
                                },
                                onEditAlarm = { alarmId ->
                                    navController.navigate(Route.AlarmEdit.create(alarmId))
                                },
                                onExploreRadio = {
                                    navController.navigate(Route.RadioExplorer.create("general"))
                                }
                            )
                        }

                        // Radio Explorer Screen
                        composable(
                            Route.RadioExplorer.route,
                            arguments = listOf(navArgument("mode") { defaultValue = "general" })
                        ) { backStackEntry ->
                            val mode = backStackEntry.arguments?.getString("mode") ?: "general"
                            val isSelectMode = mode == "select"

                            RadioExplorerScreen(
                                viewModel = radioViewModel,
                                isSelectMode = isSelectMode,
                                onStationSelected = { station ->
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("selected_station_uuid", station.stationUuid)
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("selected_station_name", station.name)
                                    com.hotbell.radio.player.RadioPlayerManager.stop(applicationContext)
                                    navController.popBackStack()
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // Alarm Edit Screen
                        composable(
                            Route.AlarmEdit.route,
                            arguments = listOf(
                                navArgument("alarmId") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val alarmId = backStackEntry.arguments?.getString("alarmId")
                            val alarmEditViewModel: AlarmEditViewModel = viewModel()

                            // Receive station selection result
                            val savedStateHandle = backStackEntry.savedStateHandle
                            val selectedUuidResult = savedStateHandle.getStateFlow<String?>("selected_station_uuid", null)
                            val selectedNameResult = savedStateHandle.getStateFlow<String?>("selected_station_name", null)

                            val selectedUuid by selectedUuidResult.collectAsState()
                            val selectedName by selectedNameResult.collectAsState()

                            LaunchedEffect(selectedUuid, selectedName) {
                                if (selectedUuid != null && selectedName != null) {
                                    alarmEditViewModel.setStation(selectedUuid!!, selectedName!!)
                                    savedStateHandle.remove<String>("selected_station_uuid")
                                    savedStateHandle.remove<String>("selected_station_name")
                                }
                            }

                            AlarmEditScreen(
                                viewModel = alarmEditViewModel,
                                alarmId = alarmId,
                                onSelectStation = {
                                    navController.navigate(Route.RadioExplorer.create("select"))
                                },
                                onDone = {
                                    navController.popBackStack(Route.Home.route, false)
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent().apply {
                    action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
        }
    }
}
