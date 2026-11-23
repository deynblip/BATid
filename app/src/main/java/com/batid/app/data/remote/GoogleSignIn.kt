package com.batid.app.data.remote

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.batid.app.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/** Result of the Google account picker. Both null = the user closed the picker. */
data class GoogleTokenResult(val idToken: String? = null, val error: String? = null)

/**
 * Task 12: "Continue with Google" using Credential Manager.
 * Only school accounts (g.batstate-u.edu.ph) are shown in the picker; Firebase checks the email again.
 */
object GoogleSignIn {
    private const val SCHOOL_DOMAIN = "g.batstate-u.edu.ph"

    /** Opens the Google account picker. Must be called with an Activity context (LocalContext in Compose). */
    suspend fun getIdToken(activityContext: Context): GoogleTokenResult = try {
        val option = GetSignInWithGoogleOption
            .Builder(activityContext.getString(R.string.default_web_client_id))
            .setHostedDomainFilter(SCHOOL_DOMAIN)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
        val credential = CredentialManager.create(activityContext)
            .getCredential(activityContext, request)
            .credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            GoogleTokenResult(idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken)
        } else {
            GoogleTokenResult(error = "Google sign-in failed. Please try again.")
        }
    } catch (e: GetCredentialCancellationException) {
        GoogleTokenResult() // closed by the user: no message
    } catch (e: NoCredentialException) {
        GoogleTokenResult(error = "No school Google account on this phone. Add it in Settings > Accounts, then try again.")
    } catch (e: GetCredentialException) {
        Log.e("GoogleSignIn", "Google sign-in failed", e)
        GoogleTokenResult(error = "Google sign-in failed. Please try again.")
    }
}