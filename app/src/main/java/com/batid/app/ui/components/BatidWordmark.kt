package com.batid.app.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.batid.app.ui.theme.ArchivoBlack
import com.batid.app.ui.theme.NavyBlue
import com.batid.app.ui.theme.RoyalBlue
import com.batid.app.ui.theme.SkyBlue

/**
 * Text version of the BATid name. PLACEHOLDER until the megaphone logo file is added;
 * then swap this for an Image(painterResource(R.drawable.batid_logo)).
 */
@Composable
fun BatidWordmark(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 40.sp,
    onDark: Boolean = false
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = if (onDark) Color.White else NavyBlue)) { append("BAT") }
            withStyle(SpanStyle(color = if (onDark) SkyBlue else RoyalBlue)) { append("id") }
        },
        modifier = modifier,
        style = TextStyle(fontFamily = ArchivoBlack, fontSize = fontSize)
    )
}
