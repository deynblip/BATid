package com.batid.app.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.batid.app.ui.components.BatidBottomBar
import com.batid.app.ui.components.BottomBarItem
import com.batid.app.ui.components.LocalIsOnline
import com.batid.app.ui.components.NoConnectionBanner
import com.batid.app.ui.components.StatusBarAppearance
import com.batid.app.ui.navigation.AdminRoutes

private val adminTabs = listOf(
    BottomBarItem(AdminRoutes.DASHBOARD, "Dashboard", Icons.Outlined.Dashboard),
    BottomBarItem(AdminRoutes.NEW, "New", Icons.Outlined.AddCircleOutline),
    BottomBarItem(AdminRoutes.MANAGE, "Manage", Icons.Outlined.EditNote)
)

/** Everything a signed-in faculty member / admin sees. Students never reach this. */
@Composable
fun AdminRoot(onSignOut: () -> Unit) {
    StatusBarAppearance(darkIcons = false)
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isOnline = LocalIsOnline.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(),
        bottomBar = {
            Column {
                NoConnectionBanner(visible = !isOnline)
                BatidBottomBar(
                    items = adminTabs,
                    currentRoute = currentRoute,
                    onSelect = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AdminRoutes.DASHBOARD,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AdminRoutes.DASHBOARD) { DashboardScreen(onSignOut = onSignOut) }
            composable(AdminRoutes.NEW) { NewAnnouncementScreen() }
            composable(AdminRoutes.MANAGE) { ManageAnnouncementsScreen() }
        }
    }
}
