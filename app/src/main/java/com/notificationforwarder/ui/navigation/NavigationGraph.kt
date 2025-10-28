package com.notificationforwarder.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.notificationforwarder.ui.screens.history.HistoryScreen
import com.notificationforwarder.ui.screens.home.HomeScreen
import com.notificationforwarder.ui.screens.webhooks.WebhookListScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Webhooks : Screen("webhooks", "Webhooks", Icons.Filled.Settings)
    object History : Screen("history", "History", Icons.Filled.List)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationGraph() {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Webhooks, Screen.History)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Webhooks.route) { WebhookListScreen(navController) }
            composable(Screen.History.route) { HistoryScreen() }
            composable("webhook/edit/{webhookId}") { backStackEntry ->
                val webhookId = backStackEntry.arguments?.getString("webhookId")?.toLongOrNull()
                com.notificationforwarder.ui.screens.webhooks.WebhookEditorScreen(
                    webhookId = webhookId,
                    navController = navController
                )
            }
            composable("webhook/new") {
                com.notificationforwarder.ui.screens.webhooks.WebhookEditorScreen(
                    webhookId = null,
                    navController = navController
                )
            }
        }
    }
}

