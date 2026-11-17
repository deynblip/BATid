package com.batid.app.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.batid.app.ui.components.BatidButton
import com.batid.app.ui.components.ComingSoonScreen

// Phase 1 placeholders. Each becomes its own file with real content in Phase 4.

@Composable
fun DashboardScreen(onSignOut: () -> Unit, modifier: Modifier = Modifier) =
    ComingSoonScreen("Dashboard", "Admin dashboard: built in Phase 4.", modifier) {
        BatidButton("Sign out", onClick = onSignOut, outlined = true)
    }

@Composable
fun NewAnnouncementScreen(modifier: Modifier = Modifier) =
    ComingSoonScreen("New Announcement", "Create announcement: built in Phase 4.", modifier)

@Composable
fun ManageAnnouncementsScreen(modifier: Modifier = Modifier) =
    ComingSoonScreen("Manage announcements", "Edit, delete, and review: built in Phase 4.", modifier)
