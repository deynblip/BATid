package com.batid.app.ui.navigation

/** Top-level routes (auth + the two role roots). */
object Routes {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val STUDENT_SIGN_IN = "student_sign_in"
    const val CREATE_ACCOUNT = "create_account"
    const val VERIFY_EMAIL = "verify_email"
    const val COMPLETE_PROFILE = "complete_profile"
    const val STUDENT_ROOT = "student"
    const val ADMIN_ROOT = "admin"
}

/** Routes inside the student side. */
object StudentRoutes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val SAVED = "saved"
    const val PROFILE = "profile"
    const val ANNOUNCEMENT_ARG = "id"
    const val ANNOUNCEMENT = "announcement/{$ANNOUNCEMENT_ARG}"
    fun announcement(id: String) = "announcement/$id"
    const val NOTIFICATIONS = "notifications"
}

/** Routes inside the faculty/admin side. */
object AdminRoutes {
    const val DASHBOARD = "dashboard"
    const val NEW = "new"
    const val MANAGE = "manage"
    const val PROFILE = "admin_profile"
    const val COMPOSE = "compose"
    const val EMERGENCY = "emergency"
    const val EDIT_ARG = "id"
    const val EDIT = "edit/{$EDIT_ARG}"
    fun edit(id: String) = "edit/$id"
    const val DETAIL = "detail/{$EDIT_ARG}"
    fun detail(id: String) = "detail/$id"
}

/** Help & info screens opened from the hamburger menu (used by both the student and admin sides). */
object HelpRoutes {
    const val HELP = "help"
    const val ABOUT = "about"
    const val CONTACT = "contact"
    const val PRIVACY = "privacy"
}