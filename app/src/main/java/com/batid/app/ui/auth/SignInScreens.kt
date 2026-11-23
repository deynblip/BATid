package com.batid.app.ui.auth

import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
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
import com.batid.app.data.model.UserProfile
import com.batid.app.data.model.UserRole
import com.batid.app.data.remote.AuthRepository
import com.batid.app.data.model.roleEnum

/** Student sign in. */
@Composable
fun StudentSignInScreen(onBack: () -> Unit, onSignedIn: (UserRole) -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var resetMessage by remember { mutableStateOf<String?>(null) }
    var confirmReset by remember { mutableStateOf(false) }

    val emailOk = isUniversityEmail(email)
    val passwordOk = password.isNotEmpty()

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Forgot password?") },
            text = {
                Text("We will send a password reset link to ${email.trim().lowercase()}. Do you want to continue?")
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    AuthRepository.sendPasswordReset(email) { error ->
                        resetMessage = error
                            ?: "If an account exists for ${email.trim().lowercase()}, we sent a password reset link to it. Please check your inbox and also your spam folder. The link can be used only once."
                    }
                }) { Text("Send link") }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text("Cancel") }
            }
        )
    }

    resetMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { resetMessage = null },
            title = { Text("Password reset") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { resetMessage = null }) { Text("OK") }
            }
        )
    }

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
            placeholder = "sr-code/name@g.batstate-u.edu.ph",
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
                            else -> onSignedIn(profile.roleEnum)
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = {
                if (!emailOk) {
                    loginError = "Enter your $AuthEmailDomain email first."
                } else {
                    confirmReset = true
                }
            }) {
                Text("Forgot password?", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

/**
 * Create account. Three kinds: Student, Student Council, Faculty / Staff.
 * Council and Faculty need a secret code (checked by the Firestore rules, never in the app).
 */
@Composable
fun CreateAccountScreen(onBack: () -> Unit, onCreated: () -> Unit, onLogIn: () -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var accountType by rememberSaveable { mutableStateOf(UserRole.STUDENT.name) }
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var program by rememberSaveable { mutableStateOf<String?>(null) }
    var year by rememberSaveable { mutableStateOf<String?>(null) }
    var section by rememberSaveable { mutableStateOf("") }
    var roleCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val isFaculty = accountType == UserRole.FACULTY.name
    val needsCode = accountType != UserRole.STUDENT.name
    val codeLabel = if (isFaculty) "Faculty code" else "Council code"

    val nameOk = fullName.isNotBlank()
    val emailOk = isUniversityEmail(email)
    // Faculty / Staff do not have a program, year level or section.
    val programOk = isFaculty || program != null
    val yearOk = isFaculty || year != null
    val sectionOk = isFaculty || section.isNotBlank()
    val codeOk = !needsCode || roleCode.isNotBlank()
    // At least 8 characters, with at least one letter AND one number.
    val passwordOk = password.length >= 8 &&
            password.any { it.isLetter() } &&
            password.any { it.isDigit() }
    val confirmOk = confirm.isNotEmpty() && confirm == password

    AuthFormScreen(onBack = onBack, showDescription = false, cardMaxHeightFraction = 0.78f) {
        Text("Create account", style = MaterialTheme.typography.titleLarge, color = InkNavy)
        Text(
            when (accountType) {
                UserRole.FACULTY.name -> "For CICS faculty and staff. You need the faculty code from the admin."
                UserRole.COUNCIL.name -> "For Student Council officers. You need the council code from the admin."
                else -> "Use your university account to get announcements for your program and year."
            },
            style = MaterialTheme.typography.bodySmall,
            color = SlateGray
        )
        Spacer(Modifier.height(16.dp))

        AuthLabel("I am a")
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterPill(
                label = "Student",
                selected = accountType == UserRole.STUDENT.name,
                onClick = { accountType = UserRole.STUDENT.name }
            )
            FilterPill(
                label = "Council",
                selected = accountType == UserRole.COUNCIL.name,
                onClick = { accountType = UserRole.COUNCIL.name }
            )
            FilterPill(
                label = "Faculty / Staff",
                selected = isFaculty,
                onClick = { accountType = UserRole.FACULTY.name }
            )
        }
        Spacer(Modifier.height(12.dp))

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

        if (!isFaculty) {
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
            Spacer(Modifier.height(6.dp))
            Text(
                "Choose carefully. Your program can only be changed by an admin. You can update your year level later in Profile.",
                style = MaterialTheme.typography.labelSmall,
                color = SlateGray
            )
            Spacer(Modifier.height(12.dp))

            AuthTextField(
                label = "Section",
                value = section,
                onValueChange = { section = it },
                placeholder = "e.g. CS - 3101",
                error = if (submitted && !sectionOk) "Enter your section." else null
            )
            Spacer(Modifier.height(12.dp))
        }

        if (needsCode) {
            AuthTextField(
                label = codeLabel,
                value = roleCode,
                onValueChange = { roleCode = it },
                placeholder = "Enter the code from the admin",
                error = if (submitted && !codeOk) "Enter the $codeLabel." else null
            )
            Spacer(Modifier.height(12.dp))
        }

        AuthTextField(
            label = "Password",
            value = password,
            onValueChange = { password = it },
            placeholder = "Create a password",
            isPassword = true,
            keyboardType = KeyboardType.Password,
            error = if (submitted && !passwordOk) "Your password does not meet the rules below." else null
        )
        PasswordRules(password)
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
                if (nameOk && emailOk && programOk && yearOk && sectionOk && codeOk && passwordOk && confirmOk) {
                    focusManager.clearFocus()
                    loading = true
                    AuthRepository.signUp(
                        email = email,
                        password = password,
                        profile = UserProfile(
                            fullName = fullName.trim(),
                            email = email.trim().lowercase(),
                            role = accountType,
                            // Faculty / Staff: default values only (the rules need a valid program and year).
                            program = if (isFaculty) Program.BSCS.name else program.orEmpty(),
                            yearLevel = if (isFaculty) YearLevel.FIRST.name else year.orEmpty(),
                            section = if (isFaculty) "" else section.trim(),
                            roleCode = if (needsCode) roleCode.trim() else ""
                        )
                    ) { error ->
                        loading = false
                        if (error == null) {
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