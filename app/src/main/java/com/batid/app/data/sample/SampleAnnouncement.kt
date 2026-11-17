package com.batid.app.data.sample

import com.batid.app.data.model.Announcement
import com.batid.app.data.model.Category
import com.batid.app.data.model.Program
import com.batid.app.data.model.PublishStatus
import com.batid.app.data.model.YearLevel
import com.batid.app.data.model.isEmergency
import com.batid.app.data.model.isFor

/**
 * TEMPORARY FAKE DATA so the UI can be seen before Firebase exists.
 * Delete this file when the real repository is connected.
 */
object SampleAnnouncements {
    private const val MIN = 60_000L
    private const val HOUR = 60 * MIN
    private const val DAY = 24 * HOUR

    fun all(now: Long = System.currentTimeMillis()): List<Announcement> = listOf(
        Announcement(
            id = "s1",
            title = "Classes suspended tomorrow, all CICS programs",
            body = "Due to severe weather, all face-to-face and online classes in the College of Informatics and Computing Sciences are suspended tomorrow, November 10.\n\nLaboratory activities and examinations scheduled for that day will be moved. Make-up schedules will be posted in this feed once confirmed.\n\nPlease stay safe and check BATid for updates.",
            category = Category.EMERGENCY.name,
            programs = listOf("BSCS", "BSIT"),
            yearLevel = YearLevel.ALL.name,
            status = PublishStatus.PUBLISHED.name,
            authorName = "CICS Dean's Office",
            createdAt = now - 10 * MIN,
            updatedAt = now
        ),
        Announcement(
            id = "s2",
            title = "Room change for CS 312 Mobile Computing",
            body = "Section CS-3101 will meet in a new room starting this Thursday. See the updated schedule.",
            category = Category.ACADEMICS.name,
            programs = listOf("BSCS"),
            yearLevel = YearLevel.THIRD.name,
            status = PublishStatus.PUBLISHED.name,
            authorName = "CICS Dean's Office",
            createdAt = now - HOUR
        ),
        Announcement(
            id = "s3",
            title = "CICS Technofusion registration is now open",
            body = "Register your team before the deadline. Slots are limited per program.",
            category = Category.EVENTS.name,
            programs = listOf("BSCS", "BSIT"),
            yearLevel = YearLevel.ALL.name,
            status = PublishStatus.PUBLISHED.name,
            authorName = "CICS Student Council",
            createdAt = now - 5 * HOUR
        ),
        Announcement(
            id = "s4",
            title = "Schedule adjustment for CS 3101 laboratory",
            body = "The laboratory session this week is moved one hour later. Bring your laptops.",
            category = Category.ACADEMICS.name,
            programs = listOf("BSCS"),
            yearLevel = YearLevel.THIRD.name,
            status = PublishStatus.PUBLISHED.name,
            authorName = "CICS Dean's Office",
            createdAt = now - 2 * DAY
        ),
        Announcement(
            id = "s5",
            title = "Reminder: update your BATid year level",
            body = "Make sure your program and year level in Profile are correct so you get the right announcements.",
            category = Category.GENERAL.name,
            programs = listOf("BSCS", "BSIT"),
            yearLevel = YearLevel.ALL.name,
            status = PublishStatus.PUBLISHED.name,
            authorName = "CICS Dean's Office",
            createdAt = now - 3 * DAY
        ),
        // The three below must NOT appear for a BSCS 3rd-year student (tests the filtering).
        Announcement(
            id = "s6",
            title = "BSIT 2nd year practicum orientation",
            body = "Orientation for BSIT 2nd-year students this Friday.",
            category = Category.ACADEMICS.name,
            programs = listOf("BSIT"),
            yearLevel = YearLevel.SECOND.name,
            status = PublishStatus.PUBLISHED.name,
            authorName = "CICS Dean's Office",
            createdAt = now - DAY
        ),
        Announcement(
            id = "s7",
            title = "BSCS 1st year freshmen assembly",
            body = "All BSCS freshmen are required to attend.",
            category = Category.GENERAL.name,
            programs = listOf("BSCS"),
            yearLevel = YearLevel.FIRST.name,
            status = PublishStatus.PUBLISHED.name,
            authorName = "CICS Dean's Office",
            createdAt = now - 4 * HOUR
        ),
        Announcement(
            id = "s8",
            title = "Draft: thesis defense schedule",
            body = "This is a draft and should not be visible to students.",
            category = Category.ACADEMICS.name,
            programs = listOf("BSCS"),
            yearLevel = YearLevel.THIRD.name,
            status = PublishStatus.DRAFT.name,
            authorName = "CICS Dean's Office",
            createdAt = now - 6 * HOUR
        )
    )

    /** What a student of [program] / [year] should see: emergencies first, then newest. */
    /** What a student of [program] / [year] should see: emergencies first, then newest. */
    fun feedFor(
        program: Program?,
        year: YearLevel,
        source: List<Announcement> = all()
    ): List<Announcement> =
        source.filter { it.isFor(program, year) }
            .sortedWith(
                compareByDescending<Announcement> { it.isEmergency }
                    .thenByDescending { it.createdAt }
            )
}