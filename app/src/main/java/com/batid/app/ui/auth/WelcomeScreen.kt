package com.batid.app.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.batid.app.R
import com.batid.app.ui.components.BatidButton
import com.batid.app.ui.components.StatusBarAppearance

/** Welcome screen: logo, peeking mascot, Log in / Create Account / Continue with Google. */
@Composable
fun WelcomeScreen(onLogIn: () -> Unit, onCreateAccount: () -> Unit) {
    StatusBarAppearance(darkIcons = true)
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.splash_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.7f))
            Image(
                painter = painterResource(R.drawable.batid_logo),
                contentDescription = "BATid logo",
                modifier = Modifier.width(220.dp)
            )

            Spacer(Modifier.weight(1f))

            Box(modifier = Modifier.fillMaxWidth()) {
                // White sheet that holds the buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 118.dp)
                        .clip(RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp))
                        .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFDDEEFF))))
                        .navigationBarsPadding()
                        .padding(start = 28.dp, end = 28.dp, top = 52.dp, bottom = 28.dp)
                ) {
                    BatidButton("Log in", onClick = onLogIn, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                    BatidButton(
                        "Create Account",
                        onClick = onCreateAccount,
                        outlined = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    OrDivider()
                    Spacer(Modifier.height(16.dp))
                    GoogleButton(
                        onClick = { showComingSoon(context, "Google sign-in will be added with Firebase.") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Mascot peeking over the top edge of the sheet
                Image(
                    painter = painterResource(R.drawable.batid_mascot),
                    contentDescription = "BATid mascot",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .width(220.dp)
                )
            }
        }
    }
}