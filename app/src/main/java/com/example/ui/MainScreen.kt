package com.example.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val titleRes: Int, val icon: @Composable () -> Unit) {
    object Home : Screen("home", R.string.nav_home, { Icon(Icons.Default.Home, null) })
    object AreaSelection : Screen("area", R.string.nav_area_selection, { Icon(Icons.Default.LocationOn, null) })
    object Queue : Screen("queue", R.string.nav_queue, { Icon(Icons.Default.List, null) })
    object History : Screen("history", R.string.nav_history, { Icon(Icons.Default.History, null) })
    object Archive : Screen("archive", R.string.nav_archive, { Icon(Icons.Default.Archive, null) })
    object Reports : Screen("reports", R.string.nav_reports, { Icon(Icons.Default.QueryStats, null) })
    object Settings : Screen("settings", R.string.nav_settings, { Icon(Icons.Default.Settings, null) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val screens = listOf(
        Screen.Home,
        Screen.AreaSelection,
        Screen.Queue,
        Screen.History,
        Screen.Archive,
        Screen.Reports,
        Screen.Settings
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                screens.forEach { screen ->
                    NavigationDrawerItem(
                        label = { Text(stringResource(screen.titleRes)) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch { drawerState.close() }
                        },
                        icon = screen.icon,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val title = screens.find { it.route == currentRoute }?.titleRes ?: R.string.app_name
                        Text(stringResource(title))
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) { HomeScreen() }
                composable(Screen.AreaSelection.route) { AreaSelectionScreen() }
                composable(Screen.Queue.route) { QueueScreen() }
                composable(Screen.History.route) { HistoryScreen() }
                composable(Screen.Archive.route) { ArchiveScreen() }
                composable(Screen.Reports.route) { ReportsScreen() }
                composable(Screen.Settings.route) { SettingsScreen() }
            }
        }
    }
}
