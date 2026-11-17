package com.batid.app.notifications

import android.Manifest
import androidx.annotation.RequiresPermission
import com.batid.app.data.model.Category
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Receives pushes. The server must send DATA-ONLY messages with:
 *   title, body, category (EMERGENCY / ACADEMICS / EVENTS / GENERAL), announcementId
 * so the app can apply the Profile switches before showing anything.
 */
class BatidMessagingService : FirebaseMessagingService() {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val title = data["title"] ?: message.notification?.title ?: return
        val body = data["body"] ?: message.notification?.body.orEmpty()
        NotificationHelper.show(
            context = this,
            title = title,
            body = body,
            category = Category.fromName(data["category"]),
            announcementId = data["announcementId"]
        )
    }

    override fun onNewToken(token: String) {
        // Topic subscriptions don't need the token. Save it to Firestore later if you want per-user pushes.
    }
}