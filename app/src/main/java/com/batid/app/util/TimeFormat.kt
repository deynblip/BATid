package com.batid.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** "Just now", "10 min ago", "1 hr ago", "2 days ago", or a date for anything older than a week. */
fun relativeTime(millis: Long, now: Long = System.currentTimeMillis()): String {
    if (millis <= 0L) return ""
    val minutes = ((now - millis).coerceAtLeast(0L)) / 60_000L
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        minutes < 60 * 24 -> "${minutes / 60} hr ago"
        minutes < 60 * 24 * 7 -> {
            val days = minutes / (60 * 24)
            if (days == 1L) "1 day ago" else "$days days ago"
        }
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))
    }
}

/** "Nov 9, 2026 · 12:38 PM" */
fun formatDateTime(millis: Long): String =
    SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()).format(Date(millis))

/** "Edited just now", "Edited 3 min ago", "Edited 2 days ago". */
fun editedLabel(updatedAt: Long, now: Long = System.currentTimeMillis()): String {
    val time = relativeTime(updatedAt, now)
    return if (time == "Just now") "Edited just now" else "Edited $time"
}
