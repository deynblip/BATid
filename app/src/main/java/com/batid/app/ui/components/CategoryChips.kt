package com.batid.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.batid.app.data.model.Category
import com.batid.app.ui.theme.ChipBlueTint
import com.batid.app.ui.theme.EmergencyRed
import com.batid.app.ui.theme.NavyBlue
import com.batid.app.ui.theme.RoyalBlue

/** Selectable pill used for category / program / year-level filters. */
@Composable
fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    large: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = if (selected) NavyBlue else Color.White,
        border = BorderStroke(1.dp, if (selected) NavyBlue else RoyalBlue)
    ) {
        Text(
            text = label,
            style = if (large) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else NavyBlue,
            modifier = Modifier.padding(
                horizontal = if (large) 18.dp else 14.dp,
                vertical = if (large) 10.dp else 6.dp
            )
        )
    }
}

/** Small non-clickable category label shown on announcement cards and details. */
@Composable
fun CategoryTag(category: Category, modifier: Modifier = Modifier) {
    val emergency = category == Category.EMERGENCY
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = if (emergency) EmergencyRed else ChipBlueTint
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (emergency) {
                Icon(
                    Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = if (emergency) category.label.uppercase() else category.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (emergency) Color.White else NavyBlue
            )
        }
    }
}
