package com.batid.app.data.model

import com.batid.app.util.relativeTime

enum class NotificationType { EMERGENCY, NEW_ANNOUNCEMENT, SYSTEM }

/** One entry in the student's notification list (the bell on Home). */
data class AppNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val timestamp: Long,
    /** When set, tapping the notification opens this announcement. */
    val announcementId: String? = null
)

val AppNotification.timeLabel: String get() = relativeTime(timestamp)

/**
 * Builds the notification list from the student's feed (newest first), plus a few app messages.
 * TEMPORARY: the SYSTEM items are fake. When Firebase is connected, announcements come from
 * Firestore and push messages (FCM) will fill this list instead.
 */
fun buildNotifications(
    feed: List<Announcement>,
    now: Long = System.currentTimeMillis()
): List<AppNotification> {
    val fromAnnouncements = feed.map { a ->
        val emergency = a.isEmergency
        AppNotification(
            id = "n_${a.id}",
            type = if (emergency) NotificationType.EMERGENCY else NotificationType.NEW_ANNOUNCEMENT,
            title = if (emergency) "Emergency alert" else "New ${a.categoryEnum.label.lowercase()} announcement",
            message = a.title,
            timestamp = a.createdAt,
            announcementId = a.id
        )
    }
    return fromAnnouncements.sortedByDescending { it.timestamp }
}