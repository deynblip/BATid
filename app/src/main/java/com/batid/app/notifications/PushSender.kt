package com.batid.app.notifications

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/**
 * Asks the free Google Apps Script sender (push-sender/Code.gs) to push a PUBLISHED
 * announcement to its FCM topics. Replaces Cloud Functions (no budget).
 */
object PushSender {

    // Web app URL from Apps Script (Deploy > Manage deployments). Not a secret.
    private const val SENDER_URL = "https://script.google.com/macros/s/AKfycbxz51SI-eW0RxEYghkXCyNUrLgH0aLsQqo6E9uvpju8wWJUGtow3YbuRsXmZ61CQX07/exec"

    /** [onResult] runs on the main thread: ok = true when the push was sent. */
    fun send(announcementId: String, onResult: (ok: Boolean, message: String) -> Unit = { _, _ -> }) {
        val main = Handler(Looper.getMainLooper())
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onResult(false, "Please log in again.")
            return
        }
        user.getIdToken(false)
            .addOnSuccessListener { result ->
                val idToken = result.token.orEmpty()
                thread {
                    val (ok, message) = try {
                        post(idToken, announcementId)
                    } catch (e: Exception) {
                        Log.e("BATID_PUSH", "sender failed", e)
                        false to "Could not reach the notification sender."
                    }
                    Log.d("BATID_PUSH", "sender result ok=$ok $message")
                    main.post { onResult(ok, message) }
                }
            }
            .addOnFailureListener { onResult(false, "Please log in again.") }
    }

    private fun post(idToken: String, announcementId: String): Pair<Boolean, String> {
        val payload = JSONObject()
            .put("idToken", idToken)
            .put("announcementId", announcementId)
            .toString()

        val conn = (URL(SENDER_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            instanceFollowRedirects = false
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("Content-Type", "application/json")
        }
        conn.outputStream.use { it.write(payload.toByteArray()) }

        val text = if (conn.responseCode in 300..399) {
            // Apps Script gives its answer on a second URL: open it with GET.
            val next = conn.getHeaderField("Location")
            conn.disconnect()
            val conn2 = (URL(next).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 30_000
            }
            readBody(conn2).also { conn2.disconnect() }
        } else {
            readBody(conn).also { conn.disconnect() }
        }

        val json = runCatching { JSONObject(text) }.getOrNull()
            ?: return false to "Unexpected reply from the notification sender."
        return if (json.optBoolean("ok")) {
            true to "Notification sent."
        } else {
            false to json.optString("error", "Notification failed.")
        }
    }

    private fun readBody(conn: HttpURLConnection): String {
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        return stream?.bufferedReader()?.use { it.readText() }.orEmpty()
    }
}