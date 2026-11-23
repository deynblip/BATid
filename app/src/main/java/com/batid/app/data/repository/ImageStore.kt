package com.batid.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

/**
 * Announcement pictures, saved as a Base64 JPEG in announcementImages/{announcementId}.
 * No Cloud Storage (Spark plan only). One picture per announcement, under ~400 KB.
 */
object ImageStore {
    private const val TAG = "ImageStore"
    private const val MAX_WIDTH = 1280
    private const val MAX_BYTES = 400 * 1024

    private fun images() = FirebaseFirestore.getInstance().collection("announcementImages")

    /** Shrinks the picked photo to a JPEG under ~400 KB. Slow: call it off the main thread. Null if it fails. */
    fun compress(context: Context, uri: Uri): ByteArray? = try {
        val bitmap = decode(context, uri)
        var quality = 70
        var bytes: ByteArray
        do {
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            bytes = out.toByteArray()
            quality -= 10
        } while (bytes.size > MAX_BYTES && quality >= 30)
        if (bytes.size > MAX_BYTES) null else bytes
    } catch (e: Exception) {
        Log.e(TAG, "Could not prepare the image", e)
        null
    }

    /** Reads the picture at most [MAX_WIDTH] px wide. */
    private fun decode(context: Context, uri: Uri): Bitmap {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val w = info.size.width
                val h = info.size.height
                if (w > MAX_WIDTH) decoder.setTargetSize(MAX_WIDTH, h * MAX_WIDTH / w)
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        }
        // Android 8.0 / 8.1
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= MAX_WIDTH) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val raw = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: throw IllegalStateException("Could not read the image")
        if (raw.width <= MAX_WIDTH) return raw
        return Bitmap.createScaledBitmap(raw, MAX_WIDTH, raw.height * MAX_WIDTH / raw.width, true)
    }

    fun toBase64(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)

    /** For the preview in the form. */
    fun preview(bytes: ByteArray): ImageBitmap? =
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()

    /** Call only AFTER the announcement itself is saved (the rules check the announcement). */
    fun save(announcementId: String, authorId: String, base64: String, onError: (String) -> Unit = {}) {
        images().document(announcementId)
            .set(mapOf("data" to base64, "authorId" to authorId))
            .addOnFailureListener { e ->
                Log.e(TAG, "Image upload failed", e)
                onError("Saved, but the image could not be uploaded. Edit the post to try again.")
            }
    }

    /** Call BEFORE deleting the announcement (the rules check the announcement). */
    fun delete(announcementId: String, onDone: () -> Unit = {}) {
        images().document(announcementId).delete()
            .addOnCompleteListener { onDone() }
    }

    /** Loads the picture once. [onResult] gets null if it is missing or can't be loaded (e.g. offline). */
    fun load(announcementId: String, onResult: (ImageBitmap?) -> Unit) {
        images().document(announcementId).get()
            .addOnSuccessListener { doc ->
                val bitmap = try {
                    doc.getString("data")?.let { data ->
                        val bytes = Base64.decode(data, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Could not decode the image", e)
                    null
                }
                onResult(bitmap)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Image load failed", e)
                onResult(null)
            }
    }
}