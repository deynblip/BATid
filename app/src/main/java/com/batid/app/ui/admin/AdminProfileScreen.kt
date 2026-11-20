package com.batid.app.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.batid.app.data.model.UserProfile
import com.batid.app.data.model.UserRole
import com.batid.app.data.model.programEnum
import com.batid.app.data.model.roleEnum
import com.batid.app.data.model.yearLevelEnum
import com.batid.app.ui.components.BatidButton
import com.batid.app.ui.components.BatidSheet
import com.batid.app.ui.theme.BorderGray
import com.batid.app.ui.theme.IceBlue
import com.batid.app.ui.theme.InkNavy
import com.batid.app.ui.theme.NavyBlue
import com.batid.app.ui.theme.NavyDark
import com.batid.app.ui.theme.ScreenBackground
import com.batid.app.ui.theme.SlateGray

/** Profile tab of the dashboard (council / faculty / admin): account info, sign out. */
@Composable
fun AdminProfileScreen(
    profile: UserProfile,
    onSignOut: () -> Unit,
    onStudentView: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showSignOutDialog by rememberSaveable { mutableStateOf(false) }
    val role = profile.roleEnum
    val roleText = when (role) {
        UserRole.ADMIN -> "Administrator"
        UserRole.FACULTY -> "Faculty / Staff"
        UserRole.COUNCIL -> "Student Council"
        UserRole.STUDENT -> "Student"
    }
    val initials = profile.fullName.trim().split(" ").filter { it.isNotBlank() }
        .take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" }

    Column(modifier = modifier.fillMaxSize()) {
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
                        roleText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        BatidSheet(modifier = Modifier.weight(1f), color = ScreenBackground) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "ACCOUNT",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkNavy,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column {
                        InfoRow("Role", roleText)
                        HorizontalDivider(color = BorderGray)
                        InfoRow("Email", profile.email)
                        if (role == UserRole.COUNCIL) {
                            HorizontalDivider(color = BorderGray)
                            InfoRow("Program", profile.programEnum?.label ?: "Not set")
                            HorizontalDivider(color = BorderGray)
                            InfoRow("Year level", profile.yearLevelEnum.label)
                            if (profile.section.isNotBlank()) {
                                HorizontalDivider(color = BorderGray)
                                InfoRow("Section", profile.section)
                            }
                        }
                    }
                }

                Spacer(Modifier.size(6.dp))
                if (onStudentView != null) {
                    BatidButton(
                        text = "Student view",
                        onClick = onStudentView,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            containerColor = Color.White,
            title = { Text("Sign out?", color = InkNavy) },
            text = { Text("Are you sure you want to sign out of BATid?", color = SlateGray) },
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
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = InkNavy)
        Spacer(Modifier.width(12.dp))
        Text(
            value,
            style = MaterialTheme.typography.labelLarge,
            color = NavyBlue,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}