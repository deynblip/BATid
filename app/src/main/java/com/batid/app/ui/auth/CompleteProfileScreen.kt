package com.batid.app.ui.auth

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.batid.app.data.model.Program
import com.batid.app.data.model.UserProfile
import com.batid.app.data.model.UserRole
import com.batid.app.data.model.YearLevel
import com.batid.app.data.remote.AuthRepository
import com.batid.app.ui.components.BatidButton
import com.batid.app.ui.components.FilterPill
import com.batid.app.ui.theme.InkNavy
import com.batid.app.ui.theme.NavyBlue
import com.batid.app.ui.theme.SlateGray

/**
 * Task 12: first time with "Continue with Google". Same choices as Create account,
 * but no email or password (Google already checked them).
 */
@Composable
fun CompleteProfileScreen(
    email: String,
    defaultName: String,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var accountType by rememberSaveable { mutableStateOf(UserRole.STUDENT.name) }
    var fullName by rememberSaveable { mutableStateOf(defaultName) }
    var program by rememberSaveable { mutableStateOf<String?>(null) }
    var year by rememberSaveable { mutableStateOf<String?>(null) }
    var section by rememberSaveable { mutableStateOf("") }
    var roleCode by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val isFaculty = accountType == UserRole.FACULTY.name
    val needsCode = accountType != UserRole.STUDENT.name
    val codeLabel = if (isFaculty) "Faculty code" else "Council code"

    val nameOk = fullName.isNotBlank()
    val programOk = isFaculty || program != null
    val yearOk = isFaculty || year != null
    val sectionOk = isFaculty || section.isNotBlank()
    val codeOk = !needsCode || roleCode.isNotBlank()

    AuthFormScreen(onBack = onCancel, showDescription = false, cardMaxHeightFraction = 0.78f) {
        Text("Complete your profile", style = MaterialTheme.typography.titleLarge, color = InkNavy)
        Text(
            "Signed in with Google as:",
            style = MaterialTheme.typography.bodySmall,
            color = SlateGray
        )
        Text(
            email,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = NavyBlue
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

        BatidButton(
            text = if (loading) "Saving..." else "Continue",
            enabled = !loading,
            onClick = {
                submitted = true
                if (nameOk && programOk && yearOk && sectionOk && codeOk) {
                    focusManager.clearFocus()
                    loading = true
                    AuthRepository.createProfile(
                        UserProfile(
                            fullName = fullName.trim(),
                            role = accountType,
                            // Faculty / Staff: default values only (the rules need a valid program and year).
                            program = if (isFaculty) Program.BSCS.name else program.orEmpty(),
                            yearLevel = if (isFaculty) YearLevel.FIRST.name else year.orEmpty(),
                            section = if (isFaculty) "" else section.trim(),
                            roleCode = if (needsCode) roleCode.trim() else ""
                        )
                    ) { error ->
                        loading = false
                        if (error == null) onDone() else showComingSoon(context, error)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel and sign out", style = MaterialTheme.typography.labelSmall)
        }
    }
}