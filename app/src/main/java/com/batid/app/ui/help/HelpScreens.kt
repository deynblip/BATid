package com.batid.app.ui.help

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.batid.app.ui.components.BatidButton
import com.batid.app.ui.components.BatidHeader
import com.batid.app.ui.components.BatidSheet
import com.batid.app.ui.theme.BorderGray
import com.batid.app.ui.theme.InkNavy
import com.batid.app.ui.theme.SlateGray

// TODO (group): palitan ng totoong email at pangalan bago ang presentation.
private const val CONTACT_EMAIL = "baTid@gmail.com"
private val TEAM_MEMBERS = listOf(
    "Dane (Lead Developer)",
    "Ace (Developer)",
    "Vianci (UI)"
)

// ───────────────────────────── Shared pieces ─────────────────────────────

/** Header (with back arrow) + white sheet + scrollable content. Used by all four info screens. */
@Composable
private fun InfoScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        BatidHeader(title = title, onBack = onBack)
        BatidSheet(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = InkNavy
            )
            Text(text = body, style = MaterialTheme.typography.bodySmall, color = SlateGray)
        }
    }
}

private fun appVersion(context: Context): String = try {
    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
} catch (e: Exception) {
    "1.0"
}

// ───────────────────────────── Help & FAQ ─────────────────────────────

private val faqItems = listOf(
    "Why can't I see an announcement?" to
            "You only see announcements for your program and year level, plus ones sent to all of CICS. " +
            "Check your year level in Profile. Your program can only be changed by an admin.",
    "How do I save an announcement?" to
            "Tap the bookmark icon on a card, or tap \"Save announcement\" inside it. " +
            "Find everything you saved in the Saved tab. Saved items stay on your phone.",
    "How do notifications work?" to
            "In Profile you can switch Academics, Events, and General notifications on or off. " +
            "Emergency alerts always notify you, even if the other switches are off.",
    "How do I change my year level?" to
            "Go to Profile, tap Year level, and choose the new one. " +
            "You will then receive announcements for that year.",
    "I'm offline. What can I see?" to
            "Announcements that were already loaded stay visible. New ones appear once you are back online.",
    "I forgot my password." to
            "On the sign-in screen, tap \"Forgot password?\", enter your school email, " +
            "and we will send you a link to reset it."
)

@Composable
fun HelpFaqScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    InfoScaffold(title = "Help & FAQ", onBack = onBack, modifier = modifier) {
        faqItems.forEach { (question, answer) -> InfoCard(question, answer) }
    }
}

// ───────────────────────────── About ─────────────────────────────

@Composable
fun AboutScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    InfoScaffold(title = "About BATid", onBack = onBack, modifier = modifier) {
        InfoCard(
            "BATid",
            "BATid is the announcement and alert app of the College of Informatics and Computing Sciences " +
                    "(CICS), Batangas State University. Students get announcements for their program and year level. " +
                    "Faculty, the Student Council, and admins publish them."
        )
        InfoCard("Version", appVersion(context))
        InfoCard("Made for", "Mobile Computing class project, CICS, Batangas State University.")
        InfoCard("Team", TEAM_MEMBERS.joinToString("\n"))
    }
}

// ───────────────────────────── Contact / Report a problem ─────────────────────────────

@Composable
fun ContactScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    InfoScaffold(title = "Contact us", onBack = onBack, modifier = modifier) {
        InfoCard(
            "Found a problem or have a suggestion?",
            "Send us an email and tell us what happened. We include your app version and phone model " +
                    "so we can fix it faster. Your name and password are never included."
        )
        BatidButton(
            text = "Send email",
            onClick = { openEmailDraft(context) },
            leadingIcon = Icons.Outlined.Email,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun openEmailDraft(context: Context) {
    val body = "Describe the problem here:\n\n\n---\n" +
            "App version: ${appVersion(context)}\n" +
            "Android: ${Build.VERSION.RELEASE}\n" +
            "Phone: ${Build.MANUFACTURER} ${Build.MODEL}"
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(CONTACT_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, "BATid: problem report")
        putExtra(Intent.EXTRA_TEXT, body)
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No email app found on this phone.", Toast.LENGTH_LONG).show()
    }
}

// ───────────────────────────── Privacy ─────────────────────────────

@Composable
fun PrivacyScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    InfoScaffold(title = "Privacy note", onBack = onBack, modifier = modifier) {
        InfoCard(
            "What BATid stores",
            "Your name, school email, program, year level, and section. Your account is protected by your password."
        )
        InfoCard(
            "Why we need it",
            "Only to show you the right announcements and to send notifications for your program and year level."
        )
        InfoCard(
            "On your phone",
            "Saved announcements and your notification choices stay on your phone."
        )
        InfoCard(
            "Sharing",
            "We do not sell or share your information. Questions? Use Contact us in the menu."
        )
    }
}