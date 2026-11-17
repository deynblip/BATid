package com.batid.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.batid.app.data.model.Announcement
import com.batid.app.data.model.Category
import com.batid.app.data.model.categoryEnum
import com.batid.app.data.model.isEmergency
import com.batid.app.data.model.metaLabel
import com.batid.app.ui.theme.BATidTheme
import com.batid.app.ui.theme.BorderGray
import com.batid.app.ui.theme.EmergencyBorder
import com.batid.app.ui.theme.EmergencyRed
import com.batid.app.ui.theme.EmergencyTint
import com.batid.app.ui.theme.InkNavy
import com.batid.app.ui.theme.NavyBlue
import com.batid.app.ui.theme.SlateGray
import com.batid.app.util.relativeTime

/** One announcement in a list. Used by the student feed, search results, and saved list. */
@Composable
fun AnnouncementCard(
    announcement: Announcement,
    isSaved: Boolean,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emergency = announcement.isEmergency
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (emergency) EmergencyTint else Color.White),
        border = BorderStroke(1.dp, if (emergency) EmergencyBorder else BorderGray)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryTag(announcement.categoryEnum)
                if (emergency) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = relativeTime(announcement.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = EmergencyRed
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onToggleSave, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isSaved) "Remove from saved" else "Save announcement",
                        tint = if (isSaved) NavyBlue else SlateGray
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = announcement.title,
                style = MaterialTheme.typography.titleMedium,
                color = InkNavy,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = announcement.body,
                style = MaterialTheme.typography.bodySmall,
                color = InkNavy,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (!emergency) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = announcement.metaLabel(),
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateGray
                )
            }
        }
    }
}

// Preview-only sample; never shown in the running app.
@Preview(showBackground = true)
@Composable
private fun AnnouncementCardPreview() {
    BATidTheme {
        AnnouncementCard(
            announcement = Announcement(
                id = "preview",
                title = "Preview announcement title",
                body = "This sample text only appears in the Android Studio preview.",
                category = Category.ACADEMICS.name,
                programs = listOf("BSCS"),
                yearLevel = "THIRD",
                createdAt = System.currentTimeMillis() - 3_600_000
            ),
            isSaved = false,
            onClick = {},
            onToggleSave = {}
        )
    }
}
