package com.batid.app.notifications

import android.content.Context
import com.batid.app.data.model.Category

/**
 * The Push Notifications switches on Profile. Stored in SharedPreferences (not Compose state)
 * so the FCM service can read them even when the app isn't open.
 */
object NotificationPrefs {
    private const val PREFS = "batid_notification_prefs"
    private const val KEY_ACTIVE = "active"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun defaultFor(category: Category) = when (category) {
        Category.EMERGENCY -> true
        Category.ACADEMICS -> true
        Category.EVENTS -> true
        Category.GENERAL -> false
    }

    /** Emergency alerts can't be turned off. */
    fun isEnabled(context: Context, category: Category): Boolean =
        category == Category.EMERGENCY ||
                prefs(context).getBoolean(category.name.lowercase(), defaultFor(category))

    fun setEnabled(context: Context, category: Category, enabled: Boolean) {
        if (category == Category.EMERGENCY) return
        prefs(context).edit().putBoolean(category.name.lowercase(), enabled).apply()
    }

    /** True only while a student is signed in; nothing is shown after sign out. */
    fun isActive(context: Context): Boolean = prefs(context).getBoolean(KEY_ACTIVE, false)

    fun setActive(context: Context, active: Boolean) {
        prefs(context).edit().putBoolean(KEY_ACTIVE, active).apply()
    }

    /** Back to defaults + inactive (called on sign out). */
    fun reset(context: Context) {
        prefs(context).edit().clear().apply()
    }
}