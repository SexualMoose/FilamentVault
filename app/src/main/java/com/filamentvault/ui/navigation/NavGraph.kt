package com.filamentvault.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.filamentvault.ui.screen.defaultsdb.DefaultsEditorScreen
import com.filamentvault.ui.screen.defaultsdb.DefaultsListScreen
import com.filamentvault.ui.screen.filamentdetail.FilamentDetailScreen
import com.filamentvault.ui.screen.filamentlist.FilamentListScreen
import com.filamentvault.ui.screen.settings.SettingsScreen

object Routes {
    const val INVENTORY = "inventory"
    const val DATABASE = "database"
    const val SETTINGS_TAB = "settings_tab"

    const val FILAMENT_DETAIL = "filament_detail/{filamentId}"
    const val FILAMENT_ADD = "filament_detail/new"

    const val DEFAULTS_EDIT = "defaults_edit/{baseId}/{overrideId}"

    fun filamentDetail(id: Long) = "filament_detail/$id"
    fun defaultsEdit(baseId: Long?, overrideId: Long?) =
        "defaults_edit/${baseId ?: -1}/${overrideId ?: -1}"
}

private sealed class Tab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Inventory : Tab(Routes.INVENTORY, "Inventory", Icons.Default.Inventory2)
    data object Database : Tab(Routes.DATABASE, "Database", Icons.Default.Storage)
    data object Settings : Tab(Routes.SETTINGS_TAB, "Settings", Icons.Default.Settings)
}

private val tabs = listOf(Tab.Inventory, Tab.Database, Tab.Settings)

@Composable
fun FilamentVaultNavGraph() {
    val rootNav: NavHostController = rememberNavController()

    NavHost(
        navController = rootNav,
        startDestination = "home"
    ) {
        composable("home") {
            HomeShell(
                onFilamentClick = { id -> rootNav.navigate(Routes.filamentDetail(id)) },
                onAddFilament = { rootNav.navigate(Routes.FILAMENT_ADD) },
                onEditDefault = { baseId, overrideId ->
                    rootNav.navigate(Routes.defaultsEdit(baseId, overrideId))
                }
            )
        }

        composable(Routes.FILAMENT_ADD) {
            FilamentDetailScreen(
                filamentId = null,
                onNavigateBack = { rootNav.popBackStack() }
            )
        }

        composable(
            route = Routes.FILAMENT_DETAIL,
            arguments = listOf(navArgument("filamentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val filamentId = backStackEntry.arguments?.getLong("filamentId")
            FilamentDetailScreen(
                filamentId = filamentId,
                onNavigateBack = { rootNav.popBackStack() }
            )
        }

        composable(
            route = Routes.DEFAULTS_EDIT,
            arguments = listOf(
                navArgument("baseId") { type = NavType.LongType },
                navArgument("overrideId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val baseId = backStackEntry.arguments?.getLong("baseId")?.takeIf { it >= 0 }
            val overrideId = backStackEntry.arguments?.getLong("overrideId")?.takeIf { it >= 0 }
            DefaultsEditorScreen(
                baseId = baseId,
                overrideId = overrideId,
                onNavigateBack = { rootNav.popBackStack() }
            )
        }
    }
}

@Composable
private fun HomeShell(
    onFilamentClick: (Long) -> Unit,
    onAddFilament: () -> Unit,
    onEditDefault: (baseId: Long?, overrideId: Long?) -> Unit
) {
    val tabNav = rememberNavController()
    val currentEntry by tabNav.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            tabNav.navigate(tab.route) {
                                popUpTo(tabNav.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = tabNav,
            startDestination = Routes.INVENTORY,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        ) {
            composable(Routes.INVENTORY) {
                FilamentListScreen(
                    onAddFilament = onAddFilament,
                    onFilamentClick = onFilamentClick,
                    onSettingsClick = {}
                )
            }
            composable(Routes.DATABASE) {
                DefaultsListScreen(onEditDefault = onEditDefault)
            }
            composable(Routes.SETTINGS_TAB) {
                SettingsScreen(onNavigateBack = null)
            }
        }
    }
}
