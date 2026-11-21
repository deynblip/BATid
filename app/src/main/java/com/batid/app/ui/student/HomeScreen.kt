package com.batid.app.ui.student

import androidx.compose.foundation.layout.height
import com.batid.app.ui.components.BatidSheet
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.ui.unit.sp
import com.batid.app.R
import com.batid.app.data.model.Announcement
import com.batid.app.data.model.Category
import com.batid.app.data.model.Program
import com.batid.app.data.model.YearLevel
import com.batid.app.data.model.categoryEnum
import com.batid.app.data.sample.SampleAnnouncements
import com.batid.app.ui.components.AnnouncementCard
import com.batid.app.ui.components.EmptyState
import com.batid.app.ui.components.FilterPill
import com.batid.app.ui.theme.BATidTheme
import com.batid.app.ui.theme.IceBlue
import com.batid.app.ui.theme.NavyBlue
import com.batid.app.ui.theme.NavyDark
import com.batid.app.ui.theme.SlateGray

private val homeCategoryFilters = listOf(Category.ACADEMICS, Category.EVENTS, Category.GENERAL)

/** Student home feed: header, greeting, category chips, and the announcement list. */
@Composable
fun HomeScreen(
    announcements: List<Announcement>,
    savedIds: Set<String>,
    onOpenAnnouncement: (String) -> Unit,
    onToggleSave: (String) -> Unit,
    onBellClick: () -> Unit,
    unreadCount: Int = 0,
    onMenuClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selected by rememberSaveable { mutableStateOf<Category?>(null) }
    val visible = remember(announcements, selected) {
        announcements.filter { selected == null || it.categoryEnum == selected }
    }

    Column(modifier = modifier.fillMaxSize()) {
        HomeHeader(onMenuClick = onMenuClick, onBellClick = onBellClick, unreadCount = unreadCount)
        BatidSheet(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { GreetingCard() }
                item {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterPill(label = "All", selected = selected == null, onClick = { selected = null })
                        homeCategoryFilters.forEach { category ->
                            FilterPill(
                                label = category.label,
                                selected = selected == category,
                                onClick = { selected = category }
                            )
                        }
                    }
                }
                if (visible.isEmpty()) {
                    item {
                        EmptyState(
                            title = "No announcements yet",
                            message = "New announcements for your program and year level will appear here."
                        )
                    }
                } else {
                    items(visible, key = { it.id }) { announcement ->
                        AnnouncementCard(
                            announcement = announcement,
                            isSaved = announcement.id in savedIds,
                            onClick = { onOpenAnnouncement(announcement.id) },
                            onToggleSave = { onToggleSave(announcement.id) }
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun HomeHeader(onMenuClick: () -> Unit, onBellClick: () -> Unit, unreadCount: Int) {
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
                    text = "CICS Announcements",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            IconButton(onClick = onBellClick) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge { Text(if (unreadCount > 9) "9+" else unreadCount.toString()) }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun GreetingCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = IceBlue
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.batid_mascot),
                contentDescription = null,
                modifier = Modifier.size(width = 86.dp, height = 60.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Hello, CICSian!",
                    style = MaterialTheme.typography.titleMedium,
                    color = NavyBlue
                )
                Text(
                    text = "Stay updated with the latest announcements.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateGray
                )
            }
        }
    }
}

// Preview-only: uses the fake sample data.
@Preview(showBackground = true, heightDp = 800)
@Composable
private fun HomeScreenPreview() {
    BATidTheme {
        HomeScreen(
            announcements = SampleAnnouncements.feedFor(Program.BSCS, YearLevel.THIRD),
            savedIds = setOf("s2"),
            onOpenAnnouncement = {},
            onToggleSave = {},
            onBellClick = {}
        )
    }
}