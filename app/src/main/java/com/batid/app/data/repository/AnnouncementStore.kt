package com.batid.app.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
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
import java.util.UUID

/**
 * TEMPORARY shared in-memory "database". Both the admin and the student side read from here,
 * so whatever the admin publishes / edits / deletes shows up for students immediately.
 * Replace the inside of these functions with Firestore calls when Firebase is connected.
 * Note: it resets to the sample data when the app process is killed.
 */
object AnnouncementStore {
    const val ADMIN_NAME = "CICS Dean's Office"

    var items: List<Announcement> by mutableStateOf(emptyList())
        private set

    fun find(id: String): Announcement? = items.firstOrNull { it.id == id }

    private var publishedListener: ListenerRegistration? = null
    private var draftsListener: ListenerRegistration? = null

    // Two separate lists from Firestore, merged into `items`.
    private var publishedList: List<Announcement> = emptyList()
    private var draftList: List<Announcement> = emptyList()

    /** Combines published + drafts into one list (no duplicates), newest first. */
    private fun refreshItems() {
        items = (publishedList + draftList)
            .distinctBy { it.id }
            .sortedByDescending { it.createdAt }
    }

    /**
     * Starts listening to PUBLISHED announcements in Firestore.
     * Safe to call many times: it only starts once.
     * (A query filtered by status == PUBLISHED is the one our security rules allow for everyone.)
     */
    fun start() {
        if (publishedListener != null) return
        publishedListener = FirebaseFirestore.getInstance()
            .collection("announcements")
            .whereEqualTo("status", PublishStatus.PUBLISHED.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AnnouncementStore", "Listen failed", error)
                    publishedListener = null
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    publishedList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Announcement::class.java)?.copy(id = doc.id)
                    }
                    refreshItems()
                }
            }
    }

    /**
     * Starts listening to DRAFT announcements (for the Manage tab).
     * ADMIN sees all drafts; FACULTY / COUNCIL see only their own (matches our security rules).
     */
    fun startDrafts(uid: String, isAdmin: Boolean) {
        if (draftsListener != null || uid.isBlank()) return
        var query: Query = FirebaseFirestore.getInstance()
            .collection("announcements")
            .whereEqualTo("status", PublishStatus.DRAFT.name)
        if (!isAdmin) {
            query = query.whereEqualTo("authorId", uid)
        }
        draftsListener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("AnnouncementStore", "Drafts listen failed", error)
                draftsListener = null
                return@addSnapshotListener
            }
            if (snapshot != null) {
                Log.d("AnnouncementStore", "drafts received: ${snapshot.size()}")
                draftList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Announcement::class.java)?.copy(id = doc.id)
                }
                refreshItems()
            }
        }
    }

    /** Stops listening and clears the list. Call this when the user signs out. */
    fun stop() {
        publishedListener?.remove()
        publishedListener = null
        draftsListener?.remove()
        draftsListener = null
        publishedList = emptyList()
        draftList = emptyList()
        items = emptyList()
    }

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