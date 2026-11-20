package com.batid.app.ui.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.batid.app.data.model.Announcement
import com.batid.app.data.model.UserRole
import com.batid.app.data.model.categoryEnum
import com.batid.app.data.model.programEnums
import com.batid.app.data.sample.SampleAnnouncements
import com.batid.app.ui.components.BatidButton
import com.batid.app.ui.components.BatidHeader
import com.batid.app.ui.components.BatidSheet
import com.batid.app.ui.components.CategoryTag
import com.batid.app.ui.components.EmptyState
import com.batid.app.ui.theme.BATidTheme
import com.batid.app.ui.theme.BorderGray
import com.batid.app.ui.theme.ChipBlueTint
import com.batid.app.ui.theme.InkNavy
import com.batid.app.ui.theme.NavyBlue
import com.batid.app.ui.theme.SlateGray
import com.batid.app.util.formatDateTime
import com.batid.app.util.relativeTime
import com.batid.app.util.editedLabel

@Composable
fun AnnouncementDetailScreen(
    announcement: Announcement?,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        BatidHeader(title = "Announcements", onBack = onBack)
        BatidSheet(modifier = Modifier.weight(1f)) {
            if (announcement == null) {
                EmptyState(
                    title = "Announcement not found",
                    message = "It may have been removed."
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        CategoryTag(announcement.categoryEnum)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = announcement.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = InkNavy
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = formatDateTime(announcement.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateGray
                        )
                        Spacer(Modifier.height(16.dp))
                        AuthorCard(announcement)
                        Spacer(Modifier.height(16.dp))
                        announcement.body.split("\n\n").forEach { paragraph ->
                            Text(
                                text = paragraph.trim(),
                                style = MaterialTheme.typography.bodySmall,
                                color = InkNavy
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                        if (announcement.updatedAt > announcement.createdAt) {
                            Text(
                                text = editedLabel(announcement.updatedAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = SlateGray
                            )
                        }
                    }
                    HorizontalDivider(color = BorderGray)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BatidButton(
                            text = if (isSaved) "Saved" else "Save announcement",
                            onClick = onToggleSave,
                            leadingIcon = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            modifier = Modifier.weight(1f)
                        )
                        BatidButton(
                            text = "Back",
                            onClick = onBack,
                            outlined = true,
                            modifier = Modifier.width(96.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthorCard(announcement: Announcement) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ChipBlueTint),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initialsOf(announcement.authorName),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = NavyBlue
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = announcement.authorName,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = InkNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val role = roleLabel(announcement.authorRole)
                if (role.isNotEmpty()) {
                    Text(text = role, style = MaterialTheme.typography.labelSmall, color = SlateGray)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                announcement.programEnums.forEach { program ->
                    Surface(shape = RoundedCornerShape(50), color = ChipBlueTint) {
                        Text(
                            text = program.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = NavyBlue,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
private fun initialsOf(name: String): String =
    name.split(" ")
        .filter { it.isNotBlank() }
        .takeLast(2)
        .joinToString("") { it.first().uppercase() }

private fun roleLabel(role: String): String = when (UserRole.fromName(role)) {
    UserRole.ADMIN -> "Administrator"
    UserRole.FACULTY -> "Faculty"
    UserRole.COUNCIL -> "Student Council"
    UserRole.STUDENT -> ""
}

// Preview-only: uses the fake sample data.
@Preview(showBackground = true, heightDp = 800)
@Composable
private fun AnnouncementDetailPreview() {
    BATidTheme {
        AnnouncementDetailScreen(
            announcement = SampleAnnouncements.all().first(),
            isSaved = false,
            onToggleSave = {},
            onBack = {}
        )
    }
}