package com.batid.app.notifications

import android.util.Log
import com.batid.app.data.model.Program
import com.batid.app.data.model.YearLevel
import com.google.firebase.messaging.FirebaseMessaging

/** Keeps the phone subscribed to exactly the FCM topics for the student's program/year. */
object PushTopics {

    /** Subscribe to the student's topics and drop all the old ones (e.g. after changing year level). */
    fun sync(program: Program?, year: YearLevel) {
        val fcm = FirebaseMessaging.getInstance()
        val wanted = if (program != null) TopicNames.subscriptionsFor(program, year) else emptySet()
        Log.d("BATID_PUSH", "sync: program=$program year=$year wanted=$wanted")
        (TopicNames.allTopics() - wanted).forEach { fcm.unsubscribeFromTopic(it) }
        wanted.forEach { topic ->
            fcm.subscribeToTopic(topic).addOnCompleteListener { task ->
                Log.d("BATID_PUSH", "subscribe $topic ok=${task.isSuccessful} ${task.exception?.message ?: ""}")
            }
        }
        fcm.token.addOnCompleteListener { task ->
            if (task.isSuccessful) Log.d("BATID_PUSH", "token=${task.result}")
            else Log.d("BATID_PUSH", "token FAILED: ${task.exception?.message}")
        }
    }

    /** Called on sign out so the phone stops receiving this student's announcements. */
    fun clear() {
        Log.d("BATID_PUSH", "clear: unsubscribing from all topics")
        val fcm = FirebaseMessaging.getInstance()
        TopicNames.allTopics().forEach { fcm.unsubscribeFromTopic(it) }
    }
}