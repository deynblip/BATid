package com.batid.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.batid.app.ui.theme.InkNavy

/** True while the device has a working internet connection. Provided once in BatidApp. */
val LocalIsOnline = compositionLocalOf { true }

/**
 * Small status strip shown while there is no connection. BATid has no offline mode, so this
 * only informs the user; it never shows stored content.
 */
@Composable
fun NoConnectionBanner(visible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(visible = visible, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(InkNavy)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.WifiOff, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = "No internet connection. Reconnect to see new announcements.",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White
            )
        }
    }
}
