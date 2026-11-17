package com.batid.app.ui.student

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.batid.app.data.model.Category
import com.batid.app.data.model.Program
import com.batid.app.data.model.UserProfile
import com.batid.app.data.model.YearLevel
import com.batid.app.data.model.programEnum
import com.batid.app.data.model.yearLevelEnum
import com.batid.app.notifications.NotificationHelper
import com.batid.app.notifications.NotificationPrefs
import com.batid.app.ui.components.BatidButton
import com.batid.app.ui.components.BatidSheet
import com.batid.app.ui.theme.BorderGray
import com.batid.app.ui.theme.EmergencyBorder
import com.batid.app.ui.theme.EmergencyRed
import com.batid.app.ui.theme.EmergencyTint
import com.batid.app.ui.theme.IceBlue
import com.batid.app.ui.theme.InkNavy
import com.batid.app.ui.theme.NavyBlue
import com.batid.app.ui.theme.NavyDark
import com.batid.app.ui.theme.ScreenBackground
import com.batid.app.ui.theme.SlateGray

private enum class GroupPicker { PROGRAM, YEAR }

/** Student profile: account info, program/year, push notification switches, sign out. */
@Composable
fun ProfileScreen(
    profile: UserProfile,
    onChangeProgram: (Program) -> Unit,
    onChangeYear: (YearLevel) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var picker by rememberSaveable { mutableStateOf<GroupPicker?>(null) }
    var showSignOutDialog by rememberSaveable { mutableStateOf(false) }

    var academics by remember { mutableStateOf(NotificationPrefs.isEnabled(context, Category.ACADEMICS)) }
    var events by remember { mutableStateOf(NotificationPrefs.isEnabled(context, Category.EVENTS)) }
    var general by remember { mutableStateOf(NotificationPrefs.isEnabled(context, Category.GENERAL)) }

    // Re-check when the user comes back from system settings.
    var notificationsAllowed by remember { mutableStateOf(NotificationHelper.canPost(context)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        notificationsAllowed = NotificationHelper.canPost(context)
    }

    val program = profile.programEnum
    val year = profile.yearLevelEnum

    Column(modifier = modifier.fillMaxSize()) {
        ProfileHeader(profile)
        BatidSheet(modifier = Modifier.weight(1f), color = ScreenBackground) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectionLabel("YOUR GROUP")
                SettingsCard {
                    GroupRow("Program", program?.label ?: "Not set") { picker = GroupPicker.PROGRAM }
                    HorizontalDivider(color = BorderGray)
                    GroupRow("Year level", year.label) { picker = GroupPicker.YEAR }
                }
                Text(
                    text = "You receive announcements sent to ${program?.label ?: "your program"}, " +
                            "${year.label}, and to all CICS students.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateGray,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(Modifier.size(6.dp))
                SectionLabel("PUSH NOTIFICATIONS")
                if (!notificationsAllowed) NotificationsOffCard { openNotificationSettings(context) }
                SettingsCard {
                    SwitchRow(
                        label = "Emergency alerts",
                        note = "Always on",
                        checked = true,
                        onCheckedChange = null, // can't be turned off
                        activeColor = EmergencyRed
                    )
                    HorizontalDivider(color = BorderGray)
                    SwitchRow("Academics", academics, {
                        academics = it
                        NotificationPrefs.setEnabled(context, Category.ACADEMICS, it)
                    })
                    HorizontalDivider(color = BorderGray)
                    SwitchRow("Events", events, {
                        events = it
                        NotificationPrefs.setEnabled(context, Category.EVENTS, it)
                    })
                    HorizontalDivider(color = BorderGray)
                    SwitchRow("General", general, {
                        general = it
                        NotificationPrefs.setEnabled(context, Category.GENERAL, it)
                    })
                }

                Spacer(Modifier.size(6.dp))
                BatidButton(
                    text = "Sign out",
                    onClick = { showSignOutDialog = true },
                    outlined = true,
                    leadingIcon = Icons.AutoMirrored.Outlined.Logout,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    when (picker) {
        GroupPicker.PROGRAM -> ChoiceDialog(
            title = "Choose your program",
            options = Program.entries.toList(),
            selected = program,
            labelOf = { it.label },
            onSelect = { onChangeProgram(it); picker = null },
            onDismiss = { picker = null }
        )
        GroupPicker.YEAR -> ChoiceDialog(
            title = "Choose your year level",
            options = YearLevel.entries.filter { it != YearLevel.ALL },
            selected = year,
            labelOf = { it.label },
            onSelect = { onChangeYear(it); picker = null },
            onDismiss = { picker = null }
        )
        null -> Unit
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            containerColor = Color.White,
            title = { Text("Sign out?", color = InkNavy) },
            text = {
                Text(
                    "Are you sure you want to sign out of BATid? You'll need to log in again to see your announcements.",
                    color = SlateGray
                )
            },
            confirmButton = {
                TextButton(onClick = { showSignOutDialog = false; onSignOut() }) {
                    Text("Sign out", color = NavyBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = SlateGray)
                }
            }
        )
    }
}

@Composable
private fun ProfileHeader(profile: UserProfile) {
    val initials = profile.fullName.trim().split(" ").filter { it.isNotBlank() }
        .take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(NavyBlue, NavyDark)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(64.dp).clip(CircleShape).background(IceBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, style = MaterialTheme.typography.titleLarge, color = NavyBlue)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    profile.fullName,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    profile.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (profile.section.isNotBlank()) {
                    Text(
                        profile.section,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = InkNavy,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(content = { content() })
    }
}

@Composable
private fun GroupRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = InkNavy, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.labelLarge, color = NavyBlue)
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = NavyBlue, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    activeColor: Color = NavyBlue,
    note: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = InkNavy)
            if (note != null) Text(note, style = MaterialTheme.typography.labelSmall, color = EmergencyRed)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = activeColor,
                checkedBorderColor = Color.Transparent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = BorderGray,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun NotificationsOffCard(onOpenSettings: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = EmergencyTint,
        border = BorderStroke(1.dp, EmergencyBorder)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Notifications are turned off for BATid, so you won't get alerts.",
                style = MaterialTheme.typography.bodySmall,
                color = InkNavy,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onOpenSettings) { Text("Turn on", color = EmergencyRed) }
        }
    }
}

@Composable
private fun <T> ChoiceDialog(
    title: String,
    options: List<T>,
    selected: T?,
    labelOf: (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text(title, color = InkNavy) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == selected,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = NavyBlue)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(labelOf(option), color = InkNavy)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = SlateGray) } }
    )
}

private fun openNotificationSettings(context: Context) {
    context.startActivity(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    )
}