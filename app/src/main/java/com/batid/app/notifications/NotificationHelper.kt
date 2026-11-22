package com.batid.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.batid.app.MainActivity
import com.batid.app.R
import com.batid.app.data.model.Category

object NotificationHelper {
    const val CHANNEL_EMERGENCY = "emergency_alerts"
    const val CHANNEL_ANNOUNCEMENTS = "announcements"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_EMERGENCY, "Emergency alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Urgent CICS alerts such as class suspensions"
                enableVibration(true)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ANNOUNCEMENTS, "Announcements", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Academics, events and general announcements"
            }
        )
    }

    /** False when the user denied the permission or turned notifications off in system settings. */
    fun canPost(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    /**
     * Shows the notification only if: a student is signed in, the category switch is on
     * (Emergency is always on), and the system allows notifications.
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun show(context: Context, title: String, body: String, category: Category, announcementId: String?) {
        if (!NotificationPrefs.isActive(context)) return
        if (!NotificationPrefs.isEnabled(context, category)) return
        if (!canPost(context)) return

        createChannels(context)
        val emergency = category == Category.EMERGENCY
        val id = (announcementId ?: (title + body)).hashCode()

        val openApp = PendingIntent.getActivity(
            context,
            id,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context,
            if (emergency) CHANNEL_EMERGENCY else CHANNEL_ANNOUNCEMENTS
        )
            .setSmallIcon(R.drawable.ic_stat_batid)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(if (emergency) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .build()

        NotificationManagerCompat.from(context).notify(id, notification)
    }
}