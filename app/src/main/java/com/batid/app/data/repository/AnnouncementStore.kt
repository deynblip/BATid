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
import com.batid.app.data.model.UserProfile
import com.batid.app.data.model.roleEnum
import com.batid.app.data.remote.AuthRepository
import com.batid.app.notifications.PushSender

object AnnouncementStore {

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

    private fun authorNameFor(me: UserProfile): String =
        if (me.roleEnum == UserRole.COUNCIL) "CICS Student Council"
        else me.fullName.ifBlank { me.email }
    fun publish(
        title: String,
        body: String,
        category: Category,
        programs: Collection<Program>,
        year: YearLevel,
        status: PublishStatus,
        linkUrl: String = "",
        imageBase64: String? = null,
        onError: (String) -> Unit = {}
    ) {
        val me = AuthRepository.currentProfile
        if (me == null) {
            onError("Please log in again.")
            return
        }
        val now = System.currentTimeMillis()
        val doc = FirebaseFirestore.getInstance().collection("announcements").document()
        val announcement = Announcement(
            id = doc.id,
            title = title,
            body = body,
            category = category.name,
            programs = Program.entries.filter { it in programs }.map { it.name },
            yearLevel = year.name,
            status = status.name,
            authorId = me.uid,
            authorName = authorNameFor(me),
            authorRole = me.role,
            createdAt = now,
            updatedAt = now,
            linkUrl = linkUrl,
            hasImage = imageBase64 != null
        )
        doc.set(announcement)
            .addOnSuccessListener {
                if (imageBase64 != null) ImageStore.save(doc.id, me.uid, imageBase64, onError)
                if (status == PublishStatus.PUBLISHED) {
                    PushSender.send(doc.id) { ok, message ->
                        if (!ok) onError("Posted, but the notification was not sent: $message")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("AnnouncementStore", "Publish failed", e)
                onError("Could not save the announcement. Please try again.")
            }
    }

    fun update(
        id: String,
        title: String,
        body: String,
        category: Category,
        programs: Collection<Program>,
        year: YearLevel,
        status: PublishStatus,
        linkUrl: String = "",
        newImageBase64: String? = null,
        removeImage: Boolean = false,
        onError: (String) -> Unit = {}
    ) {
        val old = find(id)
        if (old == null) {
            onError("Announcement not found.")
            return
        }
        val now = System.currentTimeMillis()
        // A draft being published for the first time counts as a fresh post, not an edit.
        val publishingNow = !old.isPublished && status == PublishStatus.PUBLISHED
        val changes = mapOf<String, Any>(
            "title" to title,
            "body" to body,
            "category" to category.name,
            "programs" to Program.entries.filter { it in programs }.map { it.name },
            "yearLevel" to year.name,
            "status" to status.name,
            "linkUrl" to linkUrl,
            "hasImage" to (newImageBase64 != null || (old.hasImage && !removeImage)),
            "createdAt" to (if (publishingNow) now else old.createdAt),
            "updatedAt" to now
        )
        FirebaseFirestore.getInstance().collection("announcements").document(id)
            .update(changes)
            .addOnSuccessListener {
                when {
                    newImageBase64 != null ->
                        ImageStore.save(id, AuthRepository.currentProfile?.uid.orEmpty(), newImageBase64, onError)
                    removeImage && old.hasImage -> ImageStore.delete(id)
                }
                // A draft published for the first time gets a push. Normal edits don't.
                if (publishingNow) {
                    PushSender.send(id) { ok, message ->
                        if (!ok) onError("Posted, but the notification was not sent: $message")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("AnnouncementStore", "Update failed", e)
                onError("Could not save the changes. Please try again.")
            }
    }
    fun delete(id: String, onError: (String) -> Unit = {}) {
        val deletePost = {
            FirebaseFirestore.getInstance().collection("announcements").document(id)
                .delete()
                .addOnFailureListener { e ->
                    Log.e("AnnouncementStore", "Delete failed", e)
                    onError("Could not delete the announcement. Please try again.")
                }
        }
        // The image is deleted first: its rules need the announcement to still exist.
        if (find(id)?.hasImage == true) ImageStore.delete(id) { deletePost() } else deletePost()
    }
}