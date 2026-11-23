package com.batid.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.batid.app.notifications.OpenFromPush
import com.batid.app.ui.navigation.BatidApp
import com.batid.app.ui.theme.BATidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // App opened by tapping a push: remember which post to open.
        OpenFromPush.handle(intent)
        setContent {
            BATidTheme {
                BatidApp()
            }
        }
    }

    // App was already open when the push was tapped.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        OpenFromPush.handle(intent)
    }
}