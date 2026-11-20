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
import com.batid.app.R
import com.batid.app.data.model.Announcement
import com.batid.app.data.model.Category
import com.batid.app.data.model.audienceLabel
import com.batid.app.data.model.categoryEnum
import com.batid.app.data.model.isEmergency
import com.batid.app.data.model.isPublished
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

// ───────────────────────────── Dashboard ─────────────────────────────

@Composable
fun DashboardScreen(
    announcements: List<Announcement>,
    onNewAnnouncement: () -> Unit,
    onEmergencyAlert: (() -> Unit)?,
    onSeeAll: () -> Unit,
    onOpenAnnouncement: (String) -> Unit,
    roleLabel: String = "Admin",
    modifier: Modifier = Modifier
) {
    // Same announcements students can see (published only), newest first.
    val recent = remember(announcements) {
        announcements.filter { it.isPublished }.sortedByDescending { it.createdAt }.take(3)
    }

    Column(modifier = modifier.fillMaxSize()) {
        AdminHeader(label = roleLabel)
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
                                AnnouncementStore.ADMIN_NAME,
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
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recent announcements", style = boldLabel, color = InkNavy)
                        TextButton(onClick = onSeeAll) {
                            Text("See all", style = MaterialTheme.typography.labelMedium, color = NavyBlue)
                        }
                    }
                }
                if (recent.isEmpty()) {
                    item {
                        EmptyState(
                            title = "No announcements yet",
                            message = "Announcements you publish will appear here."
                        )
                    }
                } else {
                    items(recent, key = { it.id }) { a ->
                        RecentRow(a, onClick = { onOpenAnnouncement(a.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminHeader(label: String) {
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
            Image(
                painter = painterResource(R.drawable.cics_logo),
                contentDescription = "CICS logo",
                modifier = Modifier.size(48.dp).clip(CircleShape)
            )
            Spacer(Modifier.width(12.dp))
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
                text = "To " + a.audienceLabel() + if (a.isEdited()) " · Edited" else "",
                style = MaterialTheme.typography.labelSmall,
                color = SlateGray
            )
        }
    }
}

// ───────────────────────────── Manage ─────────────────────────────

private enum class ManageFilter(val label: String) { ALL("All"), PUBLISHED("Published"), DRAFTS("Drafts") }

@Composable
fun ManageAnnouncementsScreen(
    announcements: List<Announcement>,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var filter by rememberSaveable { mutableStateOf(ManageFilter.ALL) }
    var pendingDelete by remember { mutableStateOf<Announcement?>(null) }

    val publishedCount = announcements.count { it.isPublished }
    val draftCount = announcements.size - publishedCount
    val visible = remember(announcements, filter) {
        announcements
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
        BatidHeader(title = "Manage", subtitle = "Edit or delete your announcements")
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
                                ManageFilter.ALL -> announcements.size
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
                if (visible.isEmpty()) {
                    item {
                        EmptyState(
                            title = "Nothing here yet",
                            message = "Announcements you create will show up here."
                        )
                    }
                } else {
                    items(visible, key = { it.id }) { a ->
                        ManageCard(
                            a = a,
                            onEdit = { onEdit(a.id) },
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
private fun ManageCard(a: Announcement, onEdit: () -> Unit, onDelete: () -> Unit) {
    val emergency = a.isEmergency
    Card(
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
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallAction("Edit", Icons.Outlined.Edit, NavyBlue, onEdit, Modifier.weight(1f))
                SmallAction("Delete", Icons.Outlined.DeleteOutline, EmergencyRed, onDelete, Modifier.weight(1f))
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