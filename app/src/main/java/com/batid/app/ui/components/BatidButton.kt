package com.batid.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import com.batid.app.ui.theme.NavyBlue
import com.batid.app.ui.theme.RoyalBlue

/** Pill button. Filled navy by default, or outlined blue when [outlined] is true. */
@Composable
fun BatidButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    outlined: Boolean = false,
    leadingIcon: ImageVector? = null
) {
    val content: @Composable RowScope.() -> Unit = {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
    val shape = RoundedCornerShape(50)
    val sizeModifier = modifier.heightIn(min = 48.dp)
    val padding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)

    if (outlined) {
        OutlinedButton(
            onClick = onClick,
            modifier = sizeModifier,
            enabled = enabled,
            shape = shape,
            contentPadding = padding,
            border = BorderStroke(1.5.dp, RoyalBlue),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalBlue),
            content = content
        )
    } else {
        Button(
            onClick = onClick,
            modifier = sizeModifier,
            enabled = enabled,
            shape = shape,
            contentPadding = padding,
            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue, contentColor = Color.White),
            content = content
        )
    }
}
