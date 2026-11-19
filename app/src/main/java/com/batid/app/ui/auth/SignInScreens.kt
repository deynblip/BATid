package com.batid.app.ui.auth

import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.batid.app.data.model.Program
import com.batid.app.data.model.YearLevel
import com.batid.app.ui.components.BatidButton
import com.batid.app.ui.components.FilterPill
import com.batid.app.ui.theme.InkNavy
import com.batid.app.ui.theme.SlateGray
import com.batid.app.data.local.AccountStore
import com.batid.app.data.model.UserProfile
import com.batid.app.data.model.UserRole
import com.batid.app.data.remote.AuthRepository
import com.batid.app.data.model.roleEnum

/** Student sign in. */
/** Student sign in. */
@Composable
fun StudentSignInScreen(onBack: () -> Unit, onSignedIn: () -> Unit, onAdminSignIn: () -> Unit) {
    val context = LocalContext.current
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    val emailOk = isUniversityEmail(email)
    val passwordOk = password.isNotEmpty()

    AuthFormScreen(onBack = onBack) {
        Text("Log in", style = MaterialTheme.typography.titleLarge, color = InkNavy)
        Text(
            "Use your university account to continue.",
            style = MaterialTheme.typography.bodySmall,
            color = SlateGray
        )
        Spacer(Modifier.height(16.dp))

        AuthTextField(
            label = "University email",
            value = email,
            onValueChange = { email = it; loginError = null },
            placeholder = "sr-code@g.batstate-u.edu.ph",
            keyboardType = KeyboardType.Email,
            error = when {
                submitted && !emailOk -> "Enter your $AuthEmailDomain email."
                loginError != null -> loginError
                else -> null
            }
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            label = "Password",
            value = password,
            onValueChange = { password = it; loginError = null },
            placeholder = "Enter your password",
            isPassword = true,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            error = if (submitted && !passwordOk) "Enter your password." else null
        )
        Spacer(Modifier.height(16.dp))

        BatidButton(
            text = if (loading) "Logging in..." else "Log In",
            enabled = !loading,
            onClick = {
                submitted = true
                if (emailOk && passwordOk) {
                    loading = true
                    AuthRepository.signIn(email, password) { profile, error ->
                        loading = false
                        when {
                            error != null -> loginError = error
                            profile == null -> loginError = "Something went wrong. Please try again."
                            profile.roleEnum.canPublish -> {
                                AuthRepository.signOut()
                                loginError = "This is a faculty or admin account. Use Faculty or admin log in."
                            }
                            else -> onSignedIn()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { showComingSoon(context, "Password reset will be added with Firebase.") }) {
                Text("Forgot password?", style = MaterialTheme.typography.labelSmall)
            }
            TextButton(onClick = onAdminSignIn) {
                Text("Faculty or admin log in", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

/** Faculty / admin sign in. Same layout as the student form. */
@Composable
fun AdminSignInScreen(onBack: () -> Unit, onSignedIn: () -> Unit) {
    val context = LocalContext.current
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    val emailOk = isUniversityEmail(email)
    val passwordOk = password.isNotEmpty()

    AuthFormScreen(onBack = onBack) {
        Text("Faculty / admin sign in", style = MaterialTheme.typography.titleLarge, color = InkNavy)
        Text(
            "For authorized CICS faculty and administrators.",
            style = MaterialTheme.typography.bodySmall,
            color = SlateGray
        )
        Spacer(Modifier.height(16.dp))

        AuthTextField(
            label = "University email",
            value = email,
            onValueChange = { email = it },
            placeholder = "name@g.batstate-u.edu.ph",
            keyboardType = KeyboardType.Email,
            error = if (submitted && !emailOk) "Enter your $AuthEmailDomain email." else null
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            label = "Password",
            value = password,
            onValueChange = { password = it },
            placeholder = "Enter your password",
            isPassword = true,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            error = if (submitted && !passwordOk) "Enter your password." else null
        )
        Spacer(Modifier.height(16.dp))

        BatidButton(
            text = "Log In",
            onClick = {
                submitted = true
                if (emailOk && passwordOk) onSignedIn()
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { showComingSoon(context, "Password reset will be added with Firebase.") }) {
                Text("Forgot password?", style = MaterialTheme.typography.labelSmall)
            }
            TextButton(onClick = onBack) {
                Text("Student log in", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

/** Create account for students. Same layout as the sign-in form, with extra fields. */
@Composable
fun CreateAccountScreen(onBack: () -> Unit, onCreated: () -> Unit, onLogIn: () -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var program by rememberSaveable { mutableStateOf<String?>(null) }
    var year by rememberSaveable { mutableStateOf<String?>(null) }
    var section by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val nameOk = fullName.isNotBlank()
    val emailOk = isUniversityEmail(email)
    val programOk = program != null
    val yearOk = year != null
    val sectionOk = section.isNotBlank()
    val passwordOk = password.length >= 8
    val confirmOk = confirm.isNotEmpty() && confirm == password

    AuthFormScreen(onBack = onBack, showDescription = false, cardMaxHeightFraction = 0.78f) {
        Text("Create account", style = MaterialTheme.typography.titleLarge, color = InkNavy)
        Text(
            "Use your university account to get announcements for your program and year.",
            style = MaterialTheme.typography.bodySmall,
            color = SlateGray
        )
        Spacer(Modifier.height(16.dp))

        AuthTextField(
            label = "Full name",
            value = fullName,
            onValueChange = { fullName = it },
            placeholder = "Juan Dela Cruz",
            error = if (submitted && !nameOk) "Enter your full name." else null
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            label = "University email",
            value = email,
            onValueChange = { email = it },
            placeholder = "name@g.batstate-u.edu.ph",
            keyboardType = KeyboardType.Email,
            error = if (submitted && !emailOk) "Enter your $AuthEmailDomain email." else null
        )
        Spacer(Modifier.height(12.dp))

        AuthLabel("Program")
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Program.entries.forEach { p ->
                FilterPill(label = p.label, selected = program == p.name, onClick = { program = p.name })
            }
        }
        AuthErrorText(if (submitted && !programOk) "Choose your program." else null)
        Spacer(Modifier.height(12.dp))

        AuthLabel("Year level")
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            YearLevel.entries.filter { it != YearLevel.ALL }.forEach { y ->
                FilterPill(label = y.chipLabel, selected = year == y.name, onClick = { year = y.name })
            }
        }
        AuthErrorText(if (submitted && !yearOk) "Choose your year level." else null)
        Spacer(Modifier.height(12.dp))

        AuthTextField(
            label = "Section",
            value = section,
            onValueChange = { section = it },
            placeholder = "e.g. CS - 3101",
            error = if (submitted && !sectionOk) "Enter your section." else null
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            label = "Password",
            value = password,
            onValueChange = { password = it },
            placeholder = "At least 8 characters",
            isPassword = true,
            keyboardType = KeyboardType.Password,
            error = if (submitted && !passwordOk) "Use at least 8 characters." else null
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            label = "Confirm password",
            value = confirm,
            onValueChange = { confirm = it },
            placeholder = "Enter your password again",
            isPassword = true,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            error = if (submitted && !confirmOk) "Passwords do not match." else null
        )
        Spacer(Modifier.height(16.dp))

        BatidButton(
            text = if (loading) "Creating account..." else "Create Account",
            enabled = !loading,
            onClick = {
                submitted = true
                if (nameOk && emailOk && programOk && yearOk && sectionOk && passwordOk && confirmOk) {
                    focusManager.clearFocus()
                    loading = true
                    AuthRepository.signUp(
                        email = email,
                        password = password,
                        profile = UserProfile(
                            fullName = fullName.trim(),
                            email = email.trim().lowercase(),
                            role = UserRole.STUDENT.name,
                            program = program.orEmpty(),
                            yearLevel = year.orEmpty(),
                            section = section.trim()
                        )
                    ) { error ->
                        loading = false
                        if (error == null) {
                            // Mag-log in muna pagkatapos; hindi pa tayo pumapasok sa app
                            AuthRepository.signOut()
                            onCreated()
                        } else {
                            showComingSoon(context, error)
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = onLogIn) {
                Text("Already have an account? Log in", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}