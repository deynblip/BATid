package com.batid.app.data.model

/** Stored in Firestore (`users/{uid}`). Role is what decides student vs admin screens. */
data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: String = UserRole.STUDENT.name,
    val program: String = "",
    val yearLevel: String = "",
    /** e.g. "CS - 3101" */
    val section: String = "",
    /** Secret code typed by faculty/council at sign up. The Firestore rules check it. */
    val roleCode: String = ""
)

val UserProfile.roleEnum: UserRole get() = UserRole.fromName(role)
val UserProfile.programEnum: Program? get() = Program.fromName(program)
val UserProfile.yearLevelEnum: YearLevel get() = YearLevel.fromName(yearLevel)
