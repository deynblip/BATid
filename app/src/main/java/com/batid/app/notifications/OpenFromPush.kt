package com.batid.app.notifications

import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Task 17: remembers which announcement a tapped push should open.
 * MainActivity fills it from the notification's Intent; StudentRoot opens the post and clears it.
 * The key "announcementId" is the same one the push sender puts in the FCM data.
 */
object OpenFromPush {
    const val EXTRA_ANNOUNCEMENT_ID = "announcementId"

    var announcementId by mutableStateOf<String?>(null)

    fun handle(intent: Intent?) {
        val id = intent?.getStringExtra(EXTRA_ANNOUNCEMENT_ID)
        if (!id.isNullOrBlank()) {
            announcementId = id
            intent.removeExtra(EXTRA_ANNOUNCEMENT_ID) // so it isn't opened again (e.g. after rotating)
        }
    }
}