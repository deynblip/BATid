package com.batid.app.ui.auth

import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.batid.app.R
import com.batid.app.ui.components.StatusBarAppearance
import com.batid.app.ui.theme.BorderGray
import com.batid.app.ui.theme.EmergencyRed
import com.batid.app.ui.theme.InkNavy
import com.batid.app.ui.theme.NavyBlue
import com.batid.app.ui.theme.NavyDark
import com.batid.app.ui.theme.RoyalBlue
import com.batid.app.ui.theme.SlateGray

const val AuthEmailDomain = "@g.batstate-u.edu.ph"

/** True for emails like name@g.batstate-u.edu.ph */
fun isUniversityEmail(email: String): Boolean {
    val e = email.trim().lowercase()
    return e.endsWith(AuthEmailDomain) && e.length > AuthEmailDomain.length
}

/** Small message for buttons whose real feature comes with Firebase later. */
fun showComingSoon(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

/**
 * Shared layout for Sign in, Faculty/admin sign in and Create account:
 * navy header (logo, tagline, faded mascot) + white rounded card that holds the form.
 */
@Composable
fun AuthFormScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    showDescription: Boolean = true,
    cardMaxHeightFraction: Float = 0.68f,
    cardContent: @Composable ColumnScope.() -> Unit
) {
    StatusBarAppearance(darkIcons = false)
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyBlue, NavyDark)))
    ) {
        // Faded mascot, top right
        Image(
            painter = painterResource(R.drawable.batid_mascot),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .offset(x = 50.dp, y = 60.dp)
                .width(190.dp)
                .alpha(0.22f)
        )

        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Image(
                painter = painterResource(R.drawable.batid_logo_light),
                contentDescription = "BATid logo",
                modifier = Modifier.width(170.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Stay BATid, stay informed",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            if (showDescription) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Official announcements and alerts from the College of Informatics and Computing Sciences.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
            }
        }

        // White card with the form
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .imePadding()
                .heightIn(max = screenHeight * cardMaxHeightFraction)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            content = cardContent
        )
    }
}

/** Bold small label above a field. */
@Composable
fun AuthLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = InkNavy
    )
}

/** Labeled text field with a blue outline, optional password hiding and an error message. */
@Composable
fun AuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    error: String? = null
) {
    var showPassword by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        AuthLabel(label)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall, color = SlateGray) },
            singleLine = true,
            isError = error != null,
            shape = RoundedCornerShape(10.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = InkNavy),
            visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (showPassword) "Hide password" else "Show password",
                            tint = SlateGray
                        )
                    }
                }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyBlue,
                unfocusedBorderColor = RoyalBlue,
                errorBorderColor = EmergencyRed,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                errorContainerColor = Color.White,
                cursorColor = NavyBlue
            )
        )
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.labelSmall,
                color = EmergencyRed,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/** Error text shown under pill selectors. */
@Composable
fun AuthErrorText(error: String?) {
    if (error != null) {
        Text(
            text = error,
            style = MaterialTheme.typography.labelSmall,
            color = EmergencyRed,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/** ——— or ——— */
@Composable
fun OrDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = RoyalBlue)
        Text(
            text = "or",
            style = MaterialTheme.typography.labelMedium,
            color = InkNavy,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = RoyalBlue)
    }
}

/** White "Continue with Google" button with a drawn Google "G". */
@Composable
fun GoogleButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, BorderGray),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = InkNavy)
    ) {
        GoogleGLogo(Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text("Continue with Google", style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun GoogleGLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.22f
        val inset = stroke / 2f
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val topLeft = Offset(inset, inset)
        val style = Stroke(width = stroke)
        val blue = Color(0xFF4285F4)

        drawArc(color = Color(0xFFEA4335), startAngle = 205f, sweepAngle = 110f, useCenter = false, topLeft = topLeft, size = arcSize, style = style)
        drawArc(color = Color(0xFFFBBC05), startAngle = 130f, sweepAngle = 75f, useCenter = false, topLeft = topLeft, size = arcSize, style = style)
        drawArc(color = Color(0xFF34A853), startAngle = 45f, sweepAngle = 85f, useCenter = false, topLeft = topLeft, size = arcSize, style = style)
        drawArc(color = blue, startAngle = 0f, sweepAngle = 45f, useCenter = false, topLeft = topLeft, size = arcSize, style = style)
        drawRect(
            color = blue,
            topLeft = Offset(size.width / 2f, size.height / 2f - stroke / 2f),
            size = Size(size.width / 2f, stroke)
        )
    }
}