package com.batid.app.data.local

import android.content.Context

/**
 * Saves small lists on the phone itself (works offline, no Firestore needed):
 * - saved announcement IDs (Saved tab)
 * - read notification IDs (the number on the bell)
 * Each key includes the user's uid, so two accounts on one phone don't mix.
 */
object SavedPrefs {
    private const val FILE = "batid_saved"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun getSaved(context: Context, uid: String): Set<String> =
        prefs(context).getStringSet("saved_$uid", emptySet())?.toSet() ?: emptySet()

    fun setSaved(context: Context, uid: String, ids: Set<String>) {
        prefs(context).edit().putStringSet("saved_$uid", ids).apply()
    }

    fun getRead(context: Context, uid: String): Set<String> =
        prefs(context).getStringSet("read_$uid", emptySet())?.toSet() ?: emptySet()

    fun setRead(context: Context, uid: String, ids: Set<String>) {
        prefs(context).edit().putStringSet("read_$uid", ids).apply()
    }
}