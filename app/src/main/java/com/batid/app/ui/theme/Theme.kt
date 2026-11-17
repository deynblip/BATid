package com.batid.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// The BATid design is light-only and uses fixed brand colors (no dynamic/wallpaper colors).
private val BatidColorScheme = lightColorScheme(
    primary = NavyBlue,
    onPrimary = Color.White,
    primaryContainer = IceBlue,
    onPrimaryContainer = NavyBlue,
    secondary = RoyalBlue,
    onSecondary = Color.White,
    tertiary = SkyBlue,
    background = ScreenBackground,
    onBackground = InkNavy,
    surface = Color.White,
    onSurface = InkNavy,
    surfaceVariant = IceBlue,
    onSurfaceVariant = SlateGray,
    outline = BorderGray,
    error = EmergencyRed,
    onError = Color.White
)

@Composable
fun BATidTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BatidColorScheme,
        typography = Typography,
        content = content
    )
}
