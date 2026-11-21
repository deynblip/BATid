package com.batid.app.ui.student

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.batid.app.data.local.SavedPrefs
import com.batid.app.data.model.UserProfile
import com.batid.app.data.model.UserRole
import com.batid.app.data.model.buildNotifications
import com.batid.app.data.model.feedFor
import com.batid.app.data.model.programEnum
import com.batid.app.data.model.roleEnum
import com.batid.app.data.model.yearLevelEnum
import com.batid.app.data.remote.AuthRepository
import com.batid.app.data.repository.AnnouncementStore
import com.batid.app.notifications.NotificationHelper
import com.batid.app.notifications.NotificationPrefs
import com.batid.app.notifications.PushTopics
import com.batid.app.ui.components.BatidBottomBar
import com.batid.app.ui.components.BottomBarItem
import com.batid.app.ui.components.LocalIsOnline
import com.batid.app.ui.components.NoConnectionBanner
import com.batid.app.ui.components.StatusBarAppearance
import com.batid.app.ui.navigation.StudentRoutes

private val studentTabs = listOf(
    BottomBarItem(StudentRoutes.HOME, "Home", Icons.Outlined.Home),
    BottomBarItem(StudentRoutes.SEARCH, "Search", Icons.Outlined.Search),
    BottomBarItem(StudentRoutes.SAVED, "Saved", Icons.Outlined.BookmarkBorder),
    BottomBarItem(StudentRoutes.PROFILE, "Profile", Icons.Outlined.Person)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentRoot(onSignOut: () -> Unit, onOpenCouncilDashboard: () -> Unit) {

    StatusBarAppearance(darkIcons = false)
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isOnline = LocalIsOnline.current
    val imeVisible = WindowInsets.isImeVisible
    val context = LocalContext.current
    var profile by remember { mutableStateOf(AuthRepository.currentProfile ?: UserProfile()) }
    val allAnnouncements = AnnouncementStore.items
    val studentFeed = remember(allAnnouncements, profile.program, profile.yearLevel) {
        feedFor(profile.programEnum, profile.yearLevelEnum, allAnnouncements)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        AnnouncementStore.start()
        NotificationHelper.createChannels(context)
        NotificationPrefs.setActive(context, true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    LaunchedEffect(profile.program, profile.yearLevel) {
        PushTopics.sync(profile.programEnum, profile.yearLevelEnum)
    }
    // Saved posts and "read" notifications are kept on the phone (SavedPrefs), per account.
    var savedIds by remember(profile.uid) { mutableStateOf(SavedPrefs.getSaved(context, profile.uid)) }
    var readIds by remember(profile.uid) { mutableStateOf(SavedPrefs.getRead(context, profile.uid)) }
    LaunchedEffect(savedIds) {
        if (profile.uid.isNotBlank()) SavedPrefs.setSaved(context, profile.uid, savedIds)
    }
    LaunchedEffect(readIds) {
        if (profile.uid.isNotBlank()) SavedPrefs.setRead(context, profile.uid, readIds)
    }
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
            composable(StudentRoutes.PROFILE) {
                ProfileScreen(
                    profile = profile,
                    onChangeProgram = { p ->
                        profile = profile.copy(program = p.name)
                        AuthRepository.updateProgramAndYear(profile.program, profile.yearLevel)
                    },
                    onChangeYear = { y ->
                        profile = profile.copy(yearLevel = y.name)
                        AuthRepository.updateProgramAndYear(profile.program, profile.yearLevel)
                    },
                    onSignOut = {
                        PushTopics.clear()
                        NotificationPrefs.reset(context)
                        AuthRepository.signOut()
                        onSignOut()
                    },
                    onOpenCouncilDashboard =
                        if (profile.roleEnum == UserRole.COUNCIL) onOpenCouncilDashboard else null
                )
            }
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
