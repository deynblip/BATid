package com.batid.app.data.local

import android.content.Context
import com.batid.app.data.model.UserProfile
import org.json.JSONObject

/**
 * TEMPORARY stand-in for Firebase Auth + Firestore (`users/{uid}`).
 * Keeps created accounts on the phone so Profile can show the real program/year.
 * Passwords are NOT stored or checked here; Firebase Auth will do that later.
 */
object AccountStore {
    private const val PREFS = "batid_accounts"
    private const val KEY_ACCOUNTS = "accounts"
    private const val KEY_CURRENT = "current_email"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun key(email: String) = email.trim().lowercase()

    private fun accounts(context: Context): JSONObject =
        runCatching { JSONObject(prefs(context).getString(KEY_ACCOUNTS, "{}") ?: "{}") }
            .getOrDefault(JSONObject())

    fun exists(context: Context, email: String): Boolean = accounts(context).has(key(email))

    fun register(context: Context, profile: UserProfile) = save(context, profile)

    /** Returns the account and marks it as the signed-in one, or null if no such account. */
    fun signIn(context: Context, email: String): UserProfile? {
        val profile = find(context, email) ?: return null
        prefs(context).edit().putString(KEY_CURRENT, key(email)).apply()
        return profile
    }

    /** The signed-in student (survives the app process being recreated). */
    fun current(context: Context): UserProfile? =
        prefs(context).getString(KEY_CURRENT, null)?.let { find(context, it) }

    fun update(context: Context, profile: UserProfile) = save(context, profile)

    fun signOut(context: Context) {
        prefs(context).edit().remove(KEY_CURRENT).apply()
    }

    private fun find(context: Context, email: String): UserProfile? {
        val o = accounts(context).optJSONObject(key(email)) ?: return null
        return UserProfile(
            uid = o.optString("uid"),
            fullName = o.optString("fullName"),
            email = o.optString("email"),
            role = o.optString("role"),
            program = o.optString("program"),
            yearLevel = o.optString("yearLevel"),
            section = o.optString("section")
        )
    }

    private fun save(context: Context, profile: UserProfile) {
        val all = accounts(context)
        all.put(
            key(profile.email),
            JSONObject()
                .put("uid", profile.uid)
                .put("fullName", profile.fullName)
                .put("email", key(profile.email))
                .put("role", profile.role)
                .put("program", profile.program)
                .put("yearLevel", profile.yearLevel)
                .put("section", profile.section)
        )
        prefs(context).edit().putString(KEY_ACCOUNTS, all.toString()).apply()
    }
}