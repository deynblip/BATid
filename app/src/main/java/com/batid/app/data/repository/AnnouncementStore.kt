package com.batid.app.data.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.batid.app.data.model.Announcement
import com.batid.app.data.model.Category
import com.batid.app.data.model.Program
import com.batid.app.data.model.PublishStatus
import com.batid.app.data.model.UserRole
import com.batid.app.data.model.YearLevel
import com.batid.app.data.model.isPublished
import com.batid.app.data.sample.SampleAnnouncements
import java.util.UUID

/**
 * TEMPORARY shared in-memory "database". Both the admin and the student side read from here,
 * so whatever the admin publishes / edits / deletes shows up for students immediately.
 * Replace the inside of these functions with Firestore calls when Firebase is connected.
 * Note: it resets to the sample data when the app process is killed.
 */
object AnnouncementStore {
    const val ADMIN_NAME = "CICS Dean's Office"

    var items: List<Announcement> by mutableStateOf(SampleAnnouncements.all())
        private set

    fun find(id: String): Announcement? = items.firstOrNull { it.id == id }

    fun publish(
        title: String,
        body: String,
        category: Category,
        programs: Collection<Program>,
        year: YearLevel,
        status: PublishStatus
    ) {
        val now = System.currentTimeMillis()
        val announcement = Announcement(
            id = UUID.randomUUID().toString(),
            title = title,
            body = body,
            category = category.name,
            programs = Program.entries.filter { it in programs }.map { it.name },
            yearLevel = year.name,
            status = status.name,
            authorName = ADMIN_NAME,
            authorRole = UserRole.ADMIN.name,
            createdAt = now,
            updatedAt = now
        )
        items = listOf(announcement) + items
    }

    /** Edits keep createdAt and set updatedAt = now, which is what makes "Edited 3 min ago" appear. */
    fun update(
        id: String,
        title: String,
        body: String,
        category: Category,
        programs: Collection<Program>,
        year: YearLevel,
        status: PublishStatus
    ) {
        val now = System.currentTimeMillis()
        items = items.map { old ->
            if (old.id != id) old else {
                // A draft being published for the first time counts as a fresh post, not an edit.
                val publishingNow = !old.isPublished && status == PublishStatus.PUBLISHED
                old.copy(
                    title = title,
                    body = body,
                    category = category.name,
                    programs = Program.entries.filter { it in programs }.map { it.name },
                    yearLevel = year.name,
                    status = status.name,
                    createdAt = if (publishingNow) now else old.createdAt,
                    updatedAt = now
                )
            }
        }
    }

    fun delete(id: String) {
        items = items.filterNot { it.id == id }
    }
}