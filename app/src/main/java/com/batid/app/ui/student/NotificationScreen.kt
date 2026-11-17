package com.batid.app.ui.student

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.batid.app.data.model.AppNotification
import com.batid.app.data.model.NotificationType
import com.batid.app.data.model.timeLabel
import com.batid.app.ui.components.BatidHeader
import com.batid.app.ui.components.BatidSheet
import com.batid.app.ui.components.EmptyState
import com.batid.app.ui.theme.ChipBlueTint
import com.batid.app.ui.theme.EmergencyRed
import com.batid.app.ui.theme.EmergencyTint
import com.batid.app.ui.theme.IceBlue
import com.batid.app.ui.theme.InkNavy
import com.batid.app.ui.theme.NavyBlue
import com.batid.app.ui.theme.SlateGray

/** The bell screen: newest announcements and other app messages, unread ones highlighted. */
@Composable
fun NotificationsScreen(
    notifications: List<AppNotification>,
    readIds: Set<String>,
    onOpen: (AppNotification) -> Unit,
    onMarkAllRead: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unread = notifications.count { it.id !in readIds }
    Column(modifier = modifier.fillMaxSize()) {
        BatidHeader(
            title = "Notifications",
            subtitle = if (unread == 0) "You're all caught up" else "$unread unread",
            onBack = onBack,
            trailing = {
                if (unread > 0) {
                    TextButton(onClick = onMarkAllRead) {
                        Text("Mark all read", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    }
                }
            }
        )
        BatidSheet(modifier = Modifier.weight(1f)) {
            if (notifications.isEmpty()) {
                EmptyState(
                    title = "No notifications yet",
                    message = "New announcements and alerts for you will appear here."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications, key = { it.id }) { n ->
                        NotificationRow(n, isUnread = n.id !in readIds, onClick = { onOpen(n) })
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(n: AppNotification, isUnread: Boolean, onClick: () -> Unit) {
    val emergency = n.type == NotificationType.EMERGENCY
    val icon: ImageVector = when (n.type) {
        NotificationType.EMERGENCY -> Icons.Outlined.WarningAmber
        NotificationType.NEW_ANNOUNCEMENT -> Icons.Outlined.Campaign
        NotificationType.SYSTEM -> Icons.Outlined.Info
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = when {
            emergency && isUnread -> EmergencyTint
            isUnread -> IceBlue
            else -> Color.White
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (emergency) EmergencyRed else ChipBlueTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = if (emergency) Color.White else NavyBlue)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    n.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (emergency) EmergencyRed else NavyBlue
                )
                Text(
                    n.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkNavy,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(n.timeLabel, style = MaterialTheme.typography.labelSmall, color = SlateGray)
            }
            if (isUnread) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (emergency) EmergencyRed else NavyBlue)
                )
            }
        }
    }
}