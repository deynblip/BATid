package com.batid.app.ui.auth

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.batid.app.R
import com.batid.app.ui.components.StatusBarAppearance
import com.batid.app.ui.theme.InkNavy
import kotlinx.coroutines.delay

/** Loading / landing screen. Shows the logos, then moves on to the welcome screen. */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    StatusBarAppearance(darkIcons = true)

    // Progress bar goes from 0% to 100%, then we continue to the next screen.
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(durationMillis = 2200, easing = LinearEasing))
        delay(300)
        onFinished()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background picture (your design)
        Image(
            painter = painterResource(R.drawable.splash_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            // CICS logo (top)
            Image(
                painter = painterResource(R.drawable.cics_logo),
                contentDescription = "CICS logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )

            Spacer(Modifier.weight(1f))

            // BATid logo (center)
            Image(
                painter = painterResource(R.drawable.batid_logo),
                contentDescription = "BATid logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.width(260.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Stay BATid, stay informed",
                style = MaterialTheme.typography.titleSmall,
                color = InkNavy
            )

            Spacer(Modifier.weight(1.3f))

            // "Loading..." + progress bar (bottom, on the navy waves)
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.35f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.value)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(Color.White)
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}