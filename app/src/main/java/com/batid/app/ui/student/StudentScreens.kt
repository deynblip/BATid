package com.batid.app.ui.student

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.batid.app.ui.components.BatidButton
import com.batid.app.ui.components.ComingSoonScreen

// Placeholders


@Composable
fun ProfileScreen(onSignOut: () -> Unit, modifier: Modifier = Modifier) =
    ComingSoonScreen("Profile", "Profile and notification preferences", modifier) {
        BatidButton("Sign out", onClick = onSignOut, outlined = true)
    }

