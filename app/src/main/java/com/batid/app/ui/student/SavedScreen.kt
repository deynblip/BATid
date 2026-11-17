package com.batid.app.ui.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.batid.app.data.model.Announcement
import com.batid.app.data.model.Program
import com.batid.app.data.model.YearLevel
import com.batid.app.data.sample.SampleAnnouncements
import com.batid.app.ui.components.AnnouncementCard
import com.batid.app.ui.components.BatidHeader
import com.batid.app.ui.components.BatidSheet
import com.batid.app.ui.components.EmptyState
import com.batid.app.ui.theme.BATidTheme

/**
 * Saved tab: shows only the announcements the student bookmarked.
 * [announcements] is the student's feed; [savedIds] decides which of them are shown.
 */
@Composable
fun SavedScreen(
    announcements: List<Announcement>,
    savedIds: Set<String>,
    onOpenAnnouncement: (String) -> Unit,
    onToggleSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val saved = remember(announcements, savedIds) {
        announcements.filter { it.id in savedIds }
    }

    Column(modifier = modifier.fillMaxSize()) {
        BatidHeader(
            title = "Saved",
            subtitle = if (saved.size == 1) "1 saved announcement" else "${saved.size} saved announcements"
        )
        BatidSheet(modifier = Modifier.weight(1f)) {
            if (saved.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = "No saved announcements",
                        message = "Tap the bookmark on an announcement to keep it here for later."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(saved, key = { it.id }) { announcement ->
                        AnnouncementCard(
                            announcement = announcement,
                            isSaved = true,
                            onClick = { onOpenAnnouncement(announcement.id) },
                            onToggleSave = { onToggleSave(announcement.id) }
                        )
                    }
                }
            }
        }
    }
}

// Preview-only: uses the fake sample data.
@Preview(showBackground = true, heightDp = 800)
@Composable
private fun SavedScreenPreview() {
    BATidTheme {
        SavedScreen(
            announcements = SampleAnnouncements.feedFor(Program.BSCS, YearLevel.THIRD),
            savedIds = setOf("s1", "s2"),
            onOpenAnnouncement = {},
            onToggleSave = {}
        )
    }
}