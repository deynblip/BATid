package com.batid.app.ui.student

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.batid.app.data.model.Program
import com.batid.app.data.model.YearLevel
import com.batid.app.data.sample.SampleAnnouncements
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.batid.app.ui.components.BatidBottomBar
import com.batid.app.ui.components.BottomBarItem
import com.batid.app.ui.components.LocalIsOnline
import com.batid.app.ui.components.NoConnectionBanner
import com.batid.app.ui.components.StatusBarAppearance
import com.batid.app.ui.navigation.StudentRoutes
import com.batid.app.data.model.buildNotifications
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable

private val studentTabs = listOf(
    BottomBarItem(StudentRoutes.HOME, "Home", Icons.Outlined.Home),
    BottomBarItem(StudentRoutes.SEARCH, "Search", Icons.Outlined.Search),
    BottomBarItem(StudentRoutes.SAVED, "Saved", Icons.Outlined.BookmarkBorder),
    BottomBarItem(StudentRoutes.PROFILE, "Profile", Icons.Outlined.Person)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentRoot(onSignOut: () -> Unit) {
    StatusBarAppearance(darkIcons = false)
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isOnline = LocalIsOnline.current
    val imeVisible = WindowInsets.isImeVisible

    // TEMPORARY: fake student (BSCS, 3rd year) and fake feed until Firebase is connected.
    val studentFeed = remember { SampleAnnouncements.feedFor(Program.BSCS, YearLevel.THIRD) }
    var savedIds by rememberSaveable(
        stateSaver = listSaver<Set<String>, String>(save = { it.toList() }, restore = { it.toSet() })
    ) { mutableStateOf(setOf<String>()) }
    var readIds by remember { mutableStateOf(setOf<String>()) }
    val notifications = remember(studentFeed) { buildNotifications(studentFeed) }
    val unreadCount = notifications.count { it.id !in readIds }

    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(),
        bottomBar = {
            if (!imeVisible && studentTabs.any { it.route == currentRoute }) {
                Column {
                    NoConnectionBanner(visible = !isOnline)
                    BatidBottomBar(
                        items = studentTabs,
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = StudentRoutes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(StudentRoutes.HOME) {
                HomeScreen(
                    announcements = studentFeed,
                    savedIds = savedIds,
                    onOpenAnnouncement = { id -> navController.navigate(StudentRoutes.announcement(id)) },
                    onToggleSave = { id -> savedIds = if (id in savedIds) savedIds - id else savedIds + id },
                    onBellClick = { navController.navigate(StudentRoutes.NOTIFICATIONS) { launchSingleTop = true } },
                    unreadCount = unreadCount
                )
            }
            composable(StudentRoutes.SEARCH) {
                SearchScreen(
                    announcements = studentFeed,
                    savedIds = savedIds,
                    onOpenAnnouncement = { id -> navController.navigate(StudentRoutes.announcement(id)) },
                    onToggleSave = { id -> savedIds = if (id in savedIds) savedIds - id else savedIds + id }
                )
            }
            composable(StudentRoutes.NOTIFICATIONS) {
                NotificationsScreen(
                    notifications = notifications,
                    readIds = readIds,
                    onOpen = { n ->
                        readIds = readIds + n.id
                        n.announcementId?.let { navController.navigate(StudentRoutes.announcement(it)) }
                    },
                    onMarkAllRead = { readIds = notifications.map { it.id }.toSet() },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(StudentRoutes.SAVED) {
                SavedScreen(
                    announcements = studentFeed,
                    savedIds = savedIds,
                    onOpenAnnouncement = { id -> navController.navigate(StudentRoutes.announcement(id)) },
                    onToggleSave = { id -> savedIds = if (id in savedIds) savedIds - id else savedIds + id }
                )
            }
            composable(StudentRoutes.PROFILE) { ProfileScreen(onSignOut = onSignOut) }
            composable(
                route = StudentRoutes.ANNOUNCEMENT,
                arguments = listOf(navArgument(StudentRoutes.ANNOUNCEMENT_ARG) { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString(StudentRoutes.ANNOUNCEMENT_ARG).orEmpty()
                AnnouncementDetailScreen(
                    announcement = studentFeed.firstOrNull { it.id == id },
                    isSaved = id in savedIds,
                    onToggleSave = { savedIds = if (id in savedIds) savedIds - id else savedIds + id },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
