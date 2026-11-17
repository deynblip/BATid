package com.batid.app.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.batid.app.ui.theme.ChipBlueTint
import com.batid.app.ui.theme.NavyBlue
import com.batid.app.ui.theme.SlateGray

data class BottomBarItem(val route: String, val label: String, val icon: ImageVector)

/** Bottom navigation shared by the student and admin sides; each passes its own items. */
@Composable
fun BatidBottomBar(
    items: List<BottomBarItem>,
    currentRoute: String?,
    onSelect: (String) -> Unit
) {
    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onSelect(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavyBlue,
                    selectedTextColor = NavyBlue,
                    indicatorColor = ChipBlueTint,
                    unselectedIconColor = SlateGray,
                    unselectedTextColor = SlateGray
                )
            )
        }
    }
}
