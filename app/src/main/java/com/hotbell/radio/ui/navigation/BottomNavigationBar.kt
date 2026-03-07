package com.hotbell.radio.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.hotbell.radio.ui.theme.DarkGray
import com.hotbell.radio.ui.theme.HotBellOrange
import com.hotbell.radio.ui.theme.PitchBlack

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Alarms", Route.Home.route, Icons.Default.Notifications),
        BottomNavItem("Explore", Route.RadioExplorer.create("general"), Icons.Default.Search),
        BottomNavItem("Favorites", Route.Favorites.route, Icons.Default.Favorite),
        BottomNavItem("Settings", Route.Settings.route, Icons.Default.Settings)
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    // Hide Bottom Navigation on specific screens like Select Mode or Alarm Edit
    val showBottomNav = currentRoute in items.map { it.route } || currentRoute == Route.RadioExplorer.route

    if (showBottomNav) {
        // Resolve exactly if it's the general explore mode
        val isGeneralExplore = currentRoute == Route.RadioExplorer.route &&
                navBackStackEntry.value?.arguments?.getString("mode") == "general"
        
        // Hide if we are in select mode
        val isSelectMode = currentRoute == Route.RadioExplorer.route &&
                navBackStackEntry.value?.arguments?.getString("mode") == "select"

        if (!isSelectMode) {
            NavigationBar(
                containerColor = PitchBlack,
                contentColor = Color.White
            ) {
                items.forEach { item ->
                    val isSelected = if (item.route.startsWith("radio_explorer")) {
                        isGeneralExplore
                    } else {
                        currentRoute == item.route
                    }

                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = isSelected,
                        onClick = {
                            if (!isSelected) {
                                navController.navigate(item.route) {
                                    popUpTo(Route.Home.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = HotBellOrange,
                            unselectedIconColor = DarkGray,
                            selectedTextColor = HotBellOrange,
                            unselectedTextColor = DarkGray,
                            indicatorColor = PitchBlack
                        )
                    )
                }
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
