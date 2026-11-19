package com.batid.app.data.remote

import com.batid.app.data.model.UserProfile
import com.batid.app.data.model.UserRole
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

/**
 * The file that talks sa Firebase Auth (login) at Firestore (users/{uid}).
 * Pinapalitan nito ang pansamantalang AccountStore.
 * Ang onDone ay tinatawag kapag tapos na: error message kung may mali, null kung okay.
 */
object AuthRepository {
    private val auth get() = FirebaseAuth.getInstance()
    private val db get() = FirebaseFirestore.getInstance()

    var currentProfile: UserProfile? = null
        private set

    val isSignedIn: Boolean get() = auth.currentUser != null

    fun signUp(
        email: String,
        password: String,
        profile: UserProfile,
        onDone: (error: String?) -> Unit
    ) {
        val cleanEmail = email.trim().lowercase()
        auth.createUserWithEmailAndPassword(cleanEmail, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    onDone("Something went wrong. Please try again.")
                    return@addOnSuccessListener
                }
                val saved = profile.copy(
                    uid = user.uid,
                    email = cleanEmail,
                    role = UserRole.STUDENT.name // laging STUDENT ang bagong account
                )
                db.collection("users").document(user.uid).set(saved)
                    .addOnSuccessListener {
                        user.sendEmailVerification()
                        onDone(null)
                    }
                    .addOnFailureListener { e -> onDone(friendlyError(e)) }
            }
            .addOnFailureListener { e -> onDone(friendlyError(e)) }
    }

    fun signIn(
        email: String,
        password: String,
        onDone: (profile: UserProfile?, error: String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email.trim().lowercase(), password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) {
                    onDone(null, "Something went wrong. Please try again.")
                    return@addOnSuccessListener
                }
                db.collection("users").document(uid).get()
                    .addOnSuccessListener { snap ->
                        val profile = snap.toObject(UserProfile::class.java)
                        if (profile == null) {
                            auth.signOut()
                            onDone(null, "No profile found for this account.")
                        } else {
                            currentProfile = profile
                            onDone(profile, null)
                        }
                    }
                    .addOnFailureListener { e ->
                        auth.signOut()
                        onDone(null, friendlyError(e))
                    }
            }
            .addOnFailureListener { e -> onDone(null, friendlyError(e)) }
    }

    fun loadCurrentProfile(onDone: (UserProfile?) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onDone(null)
            return
        }
        db.collection("users").document(uid).get()
            .addOnSuccessListener { snap ->
                val profile = snap.toObject(UserProfile::class.java)
                currentProfile = profile
                onDone(profile)
            }
            .addOnFailureListener { onDone(null) }
    }
    
    fun updateProgramAndYear(program: String, yearLevel: String) {
        val uid = auth.currentUser?.uid ?: return
        currentProfile = currentProfile?.copy(program = program, yearLevel = yearLevel)
        db.collection("users").document(uid)
            .update(mapOf("program" to program, "yearLevel" to yearLevel))
    }

    fun signOut() {
        currentProfile = null
        auth.signOut()
    }

    private fun friendlyError(e: Exception): String = when (e) {
        is FirebaseAuthUserCollisionException ->
            "An account with this email already exists. Please log in."
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthInvalidCredentialsException -> "Wrong email or password."
        is FirebaseNetworkException -> "No internet connection. Please try again."
        else -> e.localizedMessage ?: "Something went wrong. Please try again."
    }
}