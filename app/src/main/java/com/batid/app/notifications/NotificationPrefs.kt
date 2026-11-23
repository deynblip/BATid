package com.batid.app.notifications

import android.content.Context
import com.batid.app.data.model.Category

/**
 * The Push Notifications switches on Profile, saved PER ACCOUNT on this phone.
 * They stay after sign out and come back when the same account logs in again.
 * Stored in SharedPreferences (not Compose state) so the FCM service can read them even when the app is closed.
 */
object NotificationPrefs {
    private const val PREFS = "batid_notification_prefs"
    private const val KEY_ACTIVE_UID = "active_uid"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun activeUid(context: Context): String =
        prefs(context).getString(KEY_ACTIVE_UID, "").orEmpty()

    private fun key(uid: String, category: Category) = "${uid}_${category.name.lowercase()}"

    fun isEnabled(context: Context, category: Category): Boolean {
        if (category == Category.EMERGENCY) return true
        return prefs(context).getBoolean(key(activeUid(context), category), true)
    }

    fun setEnabled(context: Context, category: Category, enabled: Boolean) {
        if (category == Category.EMERGENCY) return
        val uid = activeUid(context)
        if (uid.isBlank()) return
        prefs(context).edit().putBoolean(key(uid, category), enabled).apply()
    }

    fun isActive(context: Context): Boolean = activeUid(context).isNotBlank()

    fun signIn(context: Context, uid: String) {
        prefs(context).edit().putString(KEY_ACTIVE_UID, uid).apply()
    }

    fun signOut(context: Context) {
        prefs(context).edit().remove(KEY_ACTIVE_UID).apply()
    }
}