package com.batid.app.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedVisibility
import com.batid.app.ui.student.FilterSection
import com.batid.app.ui.student.FilterToggleRow
import com.batid.app.R
import com.batid.app.data.model.Announcement
import com.batid.app.data.model.Category
import com.batid.app.data.model.Program
import com.batid.app.data.model.UserRole
import com.batid.app.data.model.roleEnum
import com.batid.app.data.model.YearLevel
import com.batid.app.data.model.yearLevelEnum
import com.batid.app.data.model.audienceLabel
import com.batid.app.data.model.categoryEnum
import com.batid.app.data.model.isEmergency
import com.batid.app.data.model.isPublished
import com.batid.app.data.remote.AuthRepository
import com.batid.app.data.repository.AnnouncementStore
import com.batid.app.ui.components.BatidHeader
import com.batid.app.ui.components.BatidSheet
import com.batid.app.ui.components.EmptyState
import com.batid.app.ui.components.FilterPill
import com.batid.app.ui.theme.BorderGray
import com.batid.app.ui.theme.ChipBlueTint
import com.batid.app.ui.theme.EmergencyBorder
import com.batid.app.ui.theme.EmergencyRed
import com.batid.app.ui.theme.EmergencyTint
import com.batid.app.ui.theme.IceBlue
import com.batid.app.ui.theme.InkNavy
import com.batid.app.ui.theme.NavyBlue
import com.batid.app.ui.theme.NavyDark
import com.batid.app.ui.theme.RoyalBlue
import com.batid.app.ui.theme.SlateGray
import com.batid.app.util.editedLabel
import com.batid.app.util.relativeTime

private val boldLabel @Composable get() = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)

private fun Announcement.isEdited() = isPublished && updatedAt > createdAt
private fun Announcement.matchesAudience(programFilter: String, yearFilter: String): Boolean =
    (programFilter == "ALL" || programs.any { it.equals(programFilter, ignoreCase = true) }) &&
            (yearFilter == YearLevel.ALL.name || yearLevelEnum == YearLevel.ALL || yearLevelEnum.name == yearFilter)

@Composable
private fun AudienceFilterSections(
    programFilter: String,
    onProgram: (String) -> Unit,
    yearFilter: String,
    onYear: (String) -> Unit
) {
    FilterSection("PROGRAM") {
        FilterPill("All", programFilter == "ALL", { onProgram("ALL") })
        Program.entries.forEach { p ->
            FilterPill(p.label, programFilter == p.name, { onProgram(p.name) })
        }
    }
    FilterSection("YEAR LEVEL") {
        YearLevel.entries.forEach { y ->
            FilterPill(y.chipLabel, yearFilter == y.name, { onYear(y.name) })
        }
    }
}

// ───────────────────────────── Dashboard ─────────────────────────────

@Composable
fun DashboardScreen(
    announcements: List<Announcement>,
    onNewAnnouncement: () -> Unit,
    onEmergencyAlert: (() -> Unit)?,
    onOpenAnnouncement: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    roleLabel: String = "Admin",
    modifier: Modifier = Modifier
) {

    var programFilter by rememberSaveable { mutableStateOf("ALL") }
    var yearFilter by rememberSaveable { mutableStateOf(YearLevel.ALL.name) }
    var filtersExpanded by rememberSaveable { mutableStateOf(false) }
    val activeFilters = listOf(programFilter != "ALL", yearFilter != YearLevel.ALL.name).count { it }
    val hasPublished = remember(announcements) { announcements.any { it.isPublished } }
    val feed = remember(announcements, programFilter, yearFilter) {
        announcements
            .filter { it.isPublished && it.matchesAudience(programFilter, yearFilter) }
            .sortedByDescending { it.createdAt }
    }

    Column(modifier = modifier.fillMaxSize()) {
        AdminHeader(label = roleLabel, onMenuClick = onMenuClick)
        BatidSheet(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = IceBlue
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                            Text(
                                AuthRepository.currentProfile?.fullName.orEmpty(),
                                style = MaterialTheme.typography.labelSmall,
                                color = SlateGray
                            )
                            Text("Dashboard", style = MaterialTheme.typography.titleLarge, color = NavyBlue)
                        }
                    }
                }
                item {
                    ActionCard(
                        title = "New announcement",
                        subtitle = "Send to a program, a year level, or all of CICS.",
                        icon = Icons.Outlined.Campaign,
                        iconBackground = NavyBlue,
                        container = Color.White,
                        border = BorderGray,
                        onClick = onNewAnnouncement
                    )
                }
                if (onEmergencyAlert != null) {
                    item {
                        ActionCard(
                            title = "Send an emergency alert",
                            subtitle = "Class suspensions and urgent notices.",
                            icon = Icons.Outlined.WarningAmber,
                            iconBackground = EmergencyRed,
                            container = EmergencyTint,
                            border = EmergencyBorder,
                            onClick = onEmergencyAlert
                        )
                    }
                }
                item {
                    Text(
                        "Announcements",
                        style = boldLabel,
                        color = InkNavy,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                item {
                    FilterToggleRow(
                        expanded = filtersExpanded,
                        activeCount = activeFilters,
                        onClick = { filtersExpanded = !filtersExpanded },
                        border = BorderStroke(1.dp, BorderGray)
                    )
                }
                item {
                    AnimatedVisibility(visible = filtersExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            AudienceFilterSections(
                                programFilter = programFilter,
                                onProgram = { programFilter = it },
                                yearFilter = yearFilter,
                                onYear = { yearFilter = it }
                            )
                        }
                    }
                }
                if (feed.isEmpty()) {
                    item {
                        if (hasPublished) {
                            EmptyState(
                                title = "No matching announcements",
                                message = "Try a different program or year level."
                            )
                        } else {
                            EmptyState(
                                title = "No announcements yet",
                                message = "Announcements you publish will appear here."
                            )
                        }
                    }
                } else {
                    items(feed, key = { it.id }) { a ->
                        RecentRow(a, onClick = { onOpenAnnouncement(a.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminHeader(label: String, onMenuClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(NavyBlue, NavyDark)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Menu,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Image(
                    painter = painterResource(R.drawable.batid_logo_light),
                    contentDescription = "BATid logo",
                    modifier = Modifier.size(width = 72.dp, height = 44.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBackground: Color,
    container: Color,
    border: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        border = BorderStroke(1.dp, border)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = boldLabel, color = InkNavy)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = SlateGray)
            }
        }
    }
}

@Composable
private fun RecentRow(a: Announcement, onClick: () -> Unit) {
    val emergency = a.isEmergency
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (emergency) EmergencyTint else Color.White),
        border = BorderStroke(1.dp, if (emergency) EmergencyBorder else BorderGray)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            SoftTag(a.categoryEnum)
            Spacer(Modifier.height(6.dp))
            Text(a.title, style = boldLabel, color = InkNavy, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(
                text = "To " + a.audienceLabel() + " · " + relativeTime(a.createdAt) + if (a.isEdited()) " · Edited" else "",
                style = MaterialTheme.typography.labelSmall,
                color = SlateGray
            )
        }
    }
}

// ───────────────────────────── Manage ─────────────────────────────

private enum class ManageFilter(val label: String) { ALL("All"), PUBLISHED("Published"), DRAFTS("Drafts") }

private enum class AuthorFilter(val label: String) {
    EVERYONE("Everyone"), ADMIN("Admin"), FACULTY("Faculty"), COUNCIL("Council")
}

@Composable
fun ManageAnnouncementsScreen(
    announcements: List<Announcement>,
    onEdit: (String) -> Unit,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // ADMIN sees and manages every post. FACULTY / COUNCIL see ONLY their own posts here.
    val me = AuthRepository.currentProfile
    val isAdmin = me?.roleEnum == UserRole.ADMIN
    val canManage: (Announcement) -> Boolean = { a -> isAdmin || (me != null && a.authorId == me.uid) }

    var filter by rememberSaveable { mutableStateOf(ManageFilter.ALL) }
    var pendingDelete by remember { mutableStateOf<Announcement?>(null) }

    // Admin-only extra filters (hidden behind the "Filters" row).
    var filtersExpanded by rememberSaveable { mutableStateOf(false) }
    var authorFilter by rememberSaveable { mutableStateOf(AuthorFilter.EVERYONE) }
    var programFilter by rememberSaveable { mutableStateOf("ALL") }
    var yearFilter by rememberSaveable { mutableStateOf(YearLevel.ALL.name) }
    val activeFilters = listOf(
        authorFilter != AuthorFilter.EVERYONE,
        programFilter != "ALL",
        yearFilter != YearLevel.ALL.name
    ).count { it }

    // Whose posts: own only (Faculty/Council), or all + chips (Admin).
    val scoped = remember(announcements, isAdmin, me?.uid, authorFilter, programFilter, yearFilter) {
        if (!isAdmin) {
            announcements.filter { me != null && it.authorId == me.uid }
        } else {
            announcements.filter { a ->
                val authorOk = when (authorFilter) {
                    AuthorFilter.EVERYONE -> true
                    AuthorFilter.ADMIN -> a.authorId == me?.uid
                    AuthorFilter.FACULTY -> a.authorRole.equals(UserRole.FACULTY.name, ignoreCase = true)
                    AuthorFilter.COUNCIL -> a.authorRole.equals(UserRole.COUNCIL.name, ignoreCase = true)
                }
                authorOk && a.matchesAudience(programFilter, yearFilter)
            }
        }
    }

    val publishedCount = scoped.count { it.isPublished }
    val draftCount = scoped.size - publishedCount
    val visible = remember(scoped, filter) {
        scoped
            .filter {
                when (filter) {
                    ManageFilter.ALL -> true
                    ManageFilter.PUBLISHED -> it.isPublished
                    ManageFilter.DRAFTS -> !it.isPublished
                }
            }
            .sortedByDescending { it.createdAt }
    }

    Column(modifier = modifier.fillMaxSize()) {
        BatidHeader(
            title = "Manage",
            subtitle = if (isAdmin) "Edit or delete any announcement" else "Edit or delete your announcements"
        )
        BatidSheet(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ManageFilter.entries.forEach { f ->
                            val count = when (f) {
                                ManageFilter.ALL -> scoped.size
                                ManageFilter.PUBLISHED -> publishedCount
                                ManageFilter.DRAFTS -> draftCount
                            }
                            FilterPill(
                                label = "${f.label} ($count)",
                                selected = filter == f,
                                onClick = { filter = f }
                            )
                        }
                    }
                }
                // Admin only: Posted by / Program / Year filters
                if (isAdmin) {
                    item {
                        FilterToggleRow(
                            expanded = filtersExpanded,
                            activeCount = activeFilters,
                            onClick = { filtersExpanded = !filtersExpanded },
                            border = BorderStroke(1.dp, BorderGray)
                        )
                    }
                    item {
                        AnimatedVisibility(visible = filtersExpanded) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                FilterSection("POSTED BY") {
                                    AuthorFilter.entries.forEach { f ->
                                        FilterPill(f.label, authorFilter == f, { authorFilter = f })
                                    }
                                }
                                AudienceFilterSections(
                                    programFilter = programFilter,
                                    onProgram = { programFilter = it },
                                    yearFilter = yearFilter,
                                    onYear = { yearFilter = it }
                                )
                            }
                        }
                    }
                }
                if (visible.isEmpty()) {
                    item {
                        if (isAdmin && activeFilters > 0) {
                            EmptyState(
                                title = "No matching announcements",
                                message = "Try different filters."
                            )
                        } else {
                            EmptyState(
                                title = "Nothing here yet",
                                message = "Announcements you create will show up here."
                            )
                        }
                    }
                } else {
                    items(visible, key = { it.id }) { a ->
                        ManageCard(
                            a = a,
                            canManage = canManage(a),
                            showAuthor = isAdmin,
                            onEdit = { onEdit(a.id) },
                            onOpen = { onOpen(a.id) },
                            onDelete = { pendingDelete = a }
                        )
                    }
                }
            }
        }
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = Color.White,
            title = { Text("Delete announcement?", style = MaterialTheme.typography.titleMedium, color = InkNavy) },
            text = {
                Text(
                    "\"${target.title}\" will be removed for everyone, including students. This can't be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateGray
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(target.id)
                    pendingDelete = null
                }) { Text("Delete", color = EmergencyRed, style = MaterialTheme.typography.labelLarge) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancel", color = NavyBlue, style = MaterialTheme.typography.labelLarge)
                }
            }
        )
    }
}

@Composable
private fun ManageCard(
    a: Announcement,
    canManage: Boolean,
    showAuthor: Boolean = false,
    onEdit: () -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val emergency = a.isEmergency
    Card(
        // Own post (or ADMIN): edit form. Someone else's post: read-only detail.
        onClick = if (canManage) onEdit else onOpen,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (emergency) EmergencyTint else Color.White),
        border = BorderStroke(1.dp, if (emergency) EmergencyBorder else BorderGray)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SoftTag(a.categoryEnum)
                if (!a.isPublished) {
                    Spacer(Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(50), color = SlateGray) {
                        Text(
                            "Draft",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(relativeTime(a.createdAt), style = MaterialTheme.typography.labelSmall, color = SlateGray)
            }
            Spacer(Modifier.height(6.dp))
            Text(a.title, style = boldLabel, color = InkNavy, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(a.audienceLabel(), style = MaterialTheme.typography.labelSmall, color = SlateGray)
            if (a.isEdited()) {
                Text(editedLabel(a.updatedAt), style = MaterialTheme.typography.labelSmall, color = RoyalBlue)
            }
            if (showAuthor) {
                Text("By " + a.authorName, style = MaterialTheme.typography.labelSmall, color = SlateGray)
            }
            if (canManage) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallAction("Edit", Icons.Outlined.Edit, NavyBlue, onEdit, Modifier.weight(1f))
                    SmallAction("Delete", Icons.Outlined.DeleteOutline, EmergencyRed, onDelete, Modifier.weight(1f))
                }
            } else if (!showAuthor) {
                Spacer(Modifier.height(4.dp))
                Text("By " + a.authorName, style = MaterialTheme.typography.labelSmall, color = SlateGray)
            }
        }
    }
}

@Composable
private fun SmallAction(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 40.dp),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, color),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}

/** Soft category tag used on the admin side (matches the dashboard mockup). */
@Composable
internal fun SoftTag(category: Category) {
    val emergency = category == Category.EMERGENCY
    Surface(
        shape = RoundedCornerShape(50),
        color = if (emergency) Color(0xFFFFE6D1) else ChipBlueTint
    ) {
        Text(
            text = category.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (emergency) EmergencyRed else NavyBlue,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}