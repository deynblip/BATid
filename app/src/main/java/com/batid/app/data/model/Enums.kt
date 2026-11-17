package com.batid.app.data.model

/** Announcement categories shown as chips/tags. EMERGENCY is shown in red and always notifies. */
enum class Category(val label: String) {
    ACADEMICS("Academics"),
    EVENTS("Events"),
    GENERAL("General"),
    EMERGENCY("Emergency");

    companion object {
        fun fromName(name: String?): Category =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: GENERAL
    }
}

enum class Program(val label: String) {
    BSCS("BSCS"),
    BSIT("BSIT");

    companion object {
        fun fromName(name: String?): Program? =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
}

/**
 * [chipLabel] is used on filter chips ("3rd"), [label] in text ("3rd year"),
 * [topicKey] in FCM topic names.
 */
enum class YearLevel(val chipLabel: String, val label: String, val topicKey: String) {
    ALL("All", "All years", "all"),
    FIRST("1st", "1st year", "1"),
    SECOND("2nd", "2nd year", "2"),
    THIRD("3rd", "3rd year", "3"),
    FOURTH("4th", "4th year", "4");

    companion object {
        fun fromName(name: String?): YearLevel =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: ALL
    }
}

enum class UserRole {
    STUDENT, FACULTY, ADMIN;

    /** Only faculty and admins may publish and manage announcements. */
    val canPublish: Boolean get() = this != STUDENT

    companion object {
        fun fromName(name: String?): UserRole =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: STUDENT
    }
}

enum class PublishStatus { DRAFT, PUBLISHED }
