package com.batid.app.ui.admin

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.outlined.Person
import androidx.navigation.navArgument
import com.batid.app.data.model.UserProfile
import com.batid.app.data.model.UserRole
import com.batid.app.data.model.roleEnum
import com.batid.app.data.remote.AuthRepository
import com.batid.app.notifications.PushTopics
import com.batid.app.data.repository.AnnouncementStore
import com.batid.app.ui.components.BatidBottomBar
import com.batid.app.ui.components.BottomBarItem
import com.batid.app.ui.components.LocalIsOnline
import com.batid.app.ui.components.NoConnectionBanner
import com.batid.app.ui.components.StatusBarAppearance
import com.batid.app.ui.navigation.AdminRoutes
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import com.batid.app.ui.help.BatidDrawerSheet
import com.batid.app.ui.help.helpDestinations
import kotlinx.coroutines.launch
import com.batid.app.ui.student.AnnouncementDetailScreen

private val adminTabs = listOf(
    BottomBarItem(AdminRoutes.DASHBOARD, "Dashboard", Icons.Outlined.Dashboard),
    BottomBarItem(AdminRoutes.NEW, "New", Icons.Outlined.AddCircleOutline),
    BottomBarItem(AdminRoutes.MANAGE, "Manage", Icons.Outlined.EditNote),
    BottomBarItem(AdminRoutes.PROFILE, "Profile", Icons.Outlined.Person)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminRoot(onSignOut: () -> Unit, onStudentView: () -> Unit = {}) {
    StatusBarAppearance(darkIcons = false)
    // Council lang ang may "Student view" na button.
    val isCouncil = AuthRepository.currentProfile?.roleEnum == UserRole.COUNCIL
    val context = LocalContext.current
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isOnline = LocalIsOnline.current
    val imeVisible = WindowInsets.isImeVisible

    // Shared with the student side: whatever changes here changes there.
    val announcements = AnnouncementStore.items
    LaunchedEffect(Unit) {
        AnnouncementStore.start()
        val me = AuthRepository.currentProfile
        if (me != null) {
            AnnouncementStore.startDrafts(me.uid, me.roleEnum == UserRole.ADMIN)
        }
        if (!isCouncil) PushTopics.clear()
    }

    fun goToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen || currentRoute == AdminRoutes.DASHBOARD,
        drawerContent = {
            BatidDrawerSheet(onSelect = { item ->
                scope.launch { drawerState.close() }
                navController.navigate(item.route) { launchSingleTop = true }
            })
        }
    ) {
        Scaffold(
            modifier = Modifier.imePadding(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(),
            bottomBar = {
                // Hide the bar on form screens (compose / emergency / edit) and while typing.
                if (!imeVisible && adminTabs.any { it.route == currentRoute }) {
                    Column {
                        NoConnectionBanner(visible = !isOnline)
                        BatidBottomBar(
                            items = adminTabs,
                            currentRoute = currentRoute,
                            onSelect = { route -> goToTab(route) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AdminRoutes.DASHBOARD,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(AdminRoutes.DASHBOARD) {
                    DashboardScreen(
                        announcements = announcements,
                        onNewAnnouncement = { navController.navigate(AdminRoutes.COMPOSE) },
                        onEmergencyAlert = if (isCouncil) null else {
                            { navController.navigate(AdminRoutes.EMERGENCY) }
                        },
                        onOpenAnnouncement = { id -> navController.navigate(AdminRoutes.detail(id)) },
                        onMenuClick = { scope.launch { drawerState.open() } },
                        roleLabel = if (isCouncil) "Council" else "Admin"
                    )
                }
                // "New" bottom tab: same form, no back arrow. After publishing, go to the dashboard.
                composable(AdminRoutes.NEW) {
                    AnnouncementFormScreen(
                        existing = null,
                        onBack = null,
                        onDone = { goToTab(AdminRoutes.DASHBOARD) }
                    )
                }
                composable(AdminRoutes.MANAGE) {
                    ManageAnnouncementsScreen(
                        announcements = announcements,
                        onEdit = { id -> navController.navigate(AdminRoutes.edit(id)) },
                        onOpen = { id -> navController.navigate(AdminRoutes.detail(id)) },
                        onDelete = { id ->
                            AnnouncementStore.delete(id) { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                            Toast.makeText(context, "Announcement deleted", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                helpDestinations(onBack = { navController.popBackStack() })
                composable(AdminRoutes.PROFILE) {
                    AdminProfileScreen(
                        profile = AuthRepository.currentProfile ?: UserProfile(),
                        onSignOut = onSignOut,
                        onStudentView = if (isCouncil) onStudentView else null
                    )
                }
                // Opened from the dashboard's "New announcement" button.
                composable(AdminRoutes.COMPOSE) {
                    AnnouncementFormScreen(
                        existing = null,
                        onBack = { navController.popBackStack() },
                        onDone = { navController.popBackStack() }
                    )
                }
                composable(AdminRoutes.EMERGENCY) {
                    EmergencyAlertScreen(
                        onBack = { navController.popBackStack() },
                        onDone = { navController.popBackStack() }
                    )
                }
                composable(
                    route = AdminRoutes.EDIT,
                    arguments = listOf(navArgument(AdminRoutes.EDIT_ARG) { type = NavType.StringType })
                ) { entry ->
                    val id = entry.arguments?.getString(AdminRoutes.EDIT_ARG).orEmpty()
                    val existing = announcements.firstOrNull { it.id == id }
                    if (existing == null) {
                        // Deleted while open: just leave.
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    } else {
                        AnnouncementFormScreen(
                            existing = existing,
                            onBack = { navController.popBackStack() },
                            onDone = { navController.popBackStack() }
                        )
                    }
                }
                // Read-only detail. Edit/Delete only for ADMIN (all posts) or the post's own author.
                composable(
                    route = AdminRoutes.DETAIL,
                    arguments = listOf(navArgument(AdminRoutes.EDIT_ARG) { type = NavType.StringType })
                ) { entry ->
                    val id = entry.arguments?.getString(AdminRoutes.EDIT_ARG).orEmpty()
                    val item = announcements.firstOrNull { it.id == id }
                    val me = AuthRepository.currentProfile
                    val canManage = item != null && me != null &&
                            (me.roleEnum == UserRole.ADMIN || item.authorId == me.uid)
                    AnnouncementDetailScreen(
                        announcement = item,
                        isSaved = false,
                        onToggleSave = {},
                        onBack = { navController.popBackStack() },
                        onEdit = if (canManage) {
                            { navController.navigate(AdminRoutes.edit(id)) }
                        } else null,
                        onDelete = if (canManage) {
                            {
                                AnnouncementStore.delete(id) {
                                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                                }
                                Toast.makeText(context, "Announcement deleted", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        } else null
                    )
                }
            }
        }
    }
}