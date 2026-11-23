package com.batid.app.ui.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.batid.app.data.remote.AuthRepository
import com.batid.app.ui.components.BatidButton
import com.batid.app.ui.theme.EmergencyRed
import com.batid.app.ui.theme.InkNavy
import com.batid.app.ui.theme.NavyBlue
import com.batid.app.ui.theme.SlateGray
import kotlinx.coroutines.delay

/**
 * Shown after sign up, and every time an account that is not verified yet opens the app.
 * ADMIN and presentation accounts (skipEmailVerification) never see this screen.
 */
@Composable
fun VerifyEmailScreen(
    email: String,
    onVerified: () -> Unit,
    onSignOut: () -> Unit
) {
    var checking by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var messageIsError by remember { mutableStateOf(true) }
    var cooldown by remember { mutableIntStateOf(0) }

    // Counts down the wait before "Resend email" can be used again.
    LaunchedEffect(cooldown) {
        if (cooldown > 0) {
            delay(1000)
            cooldown -= 1
        }
    }

    AuthFormScreen(onBack = onSignOut, showDescription = false) {
        Text(
            "Verify your email",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = InkNavy
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "We sent a verification link to:",
            style = MaterialTheme.typography.bodySmall,
            color = SlateGray
        )
        Text(
            email,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = NavyBlue
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Open the email and tap the link, then come back here and tap \"I've verified\". " +
                    "If you can't find it, check your Spam folder.",
            style = MaterialTheme.typography.bodySmall,
            color = SlateGray
        )
        Spacer(Modifier.height(20.dp))

        BatidButton(
            text = if (checking) "Checking..." else "I've verified",
            enabled = !checking,
            onClick = {
                checking = true
                message = null
                AuthRepository.reloadUser { verified ->
                    checking = false
                    if (verified) {
                        onVerified()
                    } else {
                        messageIsError = true
                        message = "Not verified yet. Tap the link in the email first, then try again."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        BatidButton(
            text = if (cooldown > 0) "Resend email (${cooldown}s)" else "Resend email",
            enabled = cooldown == 0,
            outlined = true,
            onClick = {
                cooldown = 60
                message = null
                AuthRepository.resendVerification { error ->
                    messageIsError = error != null
                    message = error ?: "Email sent again. Check your Inbox and Spam."
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        message?.let {
            Spacer(Modifier.height(6.dp))
            Text(
                it,
                style = MaterialTheme.typography.labelSmall,
                color = if (messageIsError) EmergencyRed else NavyBlue
            )
        }
        Spacer(Modifier.height(4.dp))
        TextButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
            Text("Use a different account (Sign out)", style = MaterialTheme.typography.labelSmall)
        }
    }
}