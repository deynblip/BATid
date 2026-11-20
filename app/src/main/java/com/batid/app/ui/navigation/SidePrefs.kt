package com.batid.app.ui.navigation

import android.content.Context

/**
 * Remembers which side a COUNCIL account was last using (student feed or dashboard),
 * so reopening the app returns to the same side. Cleared on sign out.
 */
object SidePrefs {
    private const val PREFS = "batid_side_prefs"
    private const val KEY_COUNCIL_ADMIN_SIDE = "council_on_admin_side"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun councilOnAdminSide(context: Context): Boolean =
        prefs(context).getBoolean(KEY_COUNCIL_ADMIN_SIDE, false)

    fun setCouncilOnAdminSide(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_COUNCIL_ADMIN_SIDE, value).apply()
    }
}