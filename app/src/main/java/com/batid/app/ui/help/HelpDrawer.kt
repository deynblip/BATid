package com.batid.app.ui.help

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.batid.app.R
import com.batid.app.ui.navigation.HelpRoutes
import com.batid.app.ui.theme.InkNavy
import com.batid.app.ui.theme.NavyBlue
import com.batid.app.ui.theme.NavyDark

/** The four items of the hamburger menu. */
enum class HelpMenuItem(val label: String, val icon: ImageVector, val route: String) {
    HELP("Help & FAQ", Icons.Outlined.QuestionAnswer, HelpRoutes.HELP),
    ABOUT("About BATid", Icons.Outlined.Info, HelpRoutes.ABOUT),
    CONTACT("Contact us", Icons.Outlined.Email, HelpRoutes.CONTACT),
    PRIVACY("Privacy note", Icons.Outlined.Lock, HelpRoutes.PRIVACY)
}

/** Content of the side drawer (use it inside ModalNavigationDrawer's drawerContent). */
@Composable
fun BatidDrawerSheet(onSelect: (HelpMenuItem) -> Unit) {
    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(NavyBlue, NavyDark)))
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.batid_logo_light),
                contentDescription = "BATid logo",
                modifier = Modifier.size(width = 96.dp, height = 58.dp)
            )
            Text(
                text = "CICS Announcements",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
        Spacer(Modifier.height(8.dp))
        HelpMenuItem.entries.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.label, style = MaterialTheme.typography.labelLarge) },
                icon = { Icon(item.icon, contentDescription = null) },
                selected = false,
                onClick = { onSelect(item) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedTextColor = InkNavy,
                    unselectedIconColor = NavyBlue
                )
            )
        }
    }
}

/** Adds the four help screens to a NavHost. Call it inside NavHost { ... } on both sides. */
fun NavGraphBuilder.helpDestinations(onBack: () -> Unit) {
    composable(HelpRoutes.HELP) { HelpFaqScreen(onBack = onBack) }
    composable(HelpRoutes.ABOUT) { AboutScreen(onBack = onBack) }
    composable(HelpRoutes.CONTACT) { ContactScreen(onBack = onBack) }
    composable(HelpRoutes.PRIVACY) { PrivacyScreen(onBack = onBack) }
}