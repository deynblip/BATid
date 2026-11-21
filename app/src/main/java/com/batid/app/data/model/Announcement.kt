package com.batid.app.data.model

import com.batid.app.util.relativeTime

data class Announcement(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val category: String = Category.GENERAL.name,
    val programs: List<String> = emptyList(),
    val yearLevel: String = YearLevel.ALL.name,
    val status: String = PublishStatus.DRAFT.name,
    val authorId: String = "",
    val authorName: String = "",
    val authorRole: String = UserRole.ADMIN.name,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val linkUrl: String = ""
)

val Announcement.categoryEnum: Category get() = Category.fromName(category)
val Announcement.programEnums: List<Program> get() = programs.mapNotNull { Program.fromName(it) }
val Announcement.yearLevelEnum: YearLevel get() = YearLevel.fromName(yearLevel)
val Announcement.isEmergency: Boolean get() = categoryEnum == Category.EMERGENCY
val Announcement.isPublished: Boolean get() = status.equals(PublishStatus.PUBLISHED.name, ignoreCase = true)

/** e.g. "BSCS · 3rd year" or "BSCS, BSIT · All years" */
fun Announcement.audienceLabel(): String =
    programEnums.joinToString(", ") { it.label } + " · " + yearLevelEnum.label

/** e.g. "BSCS · 3rd year · 1 hr ago" */
fun Announcement.metaLabel(): String = audienceLabel() + " · " + relativeTime(createdAt)


/** True when a student of [program] / [year] should see this announcement. */
fun Announcement.isFor(program: Program?, year: YearLevel): Boolean =
    isPublished &&
            program != null &&
            programs.any { it.equals(program.name, ignoreCase = true) } &&
            (yearLevelEnum == YearLevel.ALL || yearLevelEnum == year)

fun feedFor(program: Program?, year: YearLevel, source: List<Announcement>): List<Announcement> =
    source.filter { it.isFor(program, year) }
        .sortedWith(
            compareByDescending<Announcement> { it.isEmergency }
                .thenByDescending { it.createdAt }
        )