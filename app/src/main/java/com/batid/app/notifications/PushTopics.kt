package com.batid.app.notifications

import com.batid.app.data.model.Program
import com.batid.app.data.model.YearLevel
import com.google.firebase.messaging.FirebaseMessaging

/** Keeps the phone subscribed to exactly the FCM topics for the student's program/year. */
object PushTopics {

    /** Subscribe to the student's topics and drop all the old ones (e.g. after changing year level). */
    fun sync(program: Program?, year: YearLevel) {
        val fcm = FirebaseMessaging.getInstance()
        val wanted = if (program != null) TopicNames.subscriptionsFor(program, year) else emptySet()
        (TopicNames.allTopics() - wanted).forEach { fcm.unsubscribeFromTopic(it) }
        wanted.forEach { fcm.subscribeToTopic(it) }
    }

    /** Called on sign out so the phone stops receiving this student's announcements. */
    fun clear() {
        val fcm = FirebaseMessaging.getInstance()
        TopicNames.allTopics().forEach { fcm.unsubscribeFromTopic(it) }
    }
}