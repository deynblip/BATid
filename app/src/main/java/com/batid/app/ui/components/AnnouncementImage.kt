package com.batid.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.batid.app.data.repository.ImageStore
import com.batid.app.ui.theme.BorderGray
import com.batid.app.ui.theme.ChipBlueTint
import com.batid.app.ui.theme.NavyBlue
import com.batid.app.ui.theme.SlateGray

/** Loads the announcement's picture once. Tap = full screen with pinch-to-zoom. */
@Composable
fun AnnouncementImage(announcementId: String, modifier: Modifier = Modifier) {
    var image by remember(announcementId) { mutableStateOf<ImageBitmap?>(null) }
    var loading by remember(announcementId) { mutableStateOf(true) }
    var zoomOpen by remember { mutableStateOf(false) }

    LaunchedEffect(announcementId) {
        loading = true
        ImageStore.load(announcementId) { result ->
            image = result
            loading = false
        }
    }

    val shape = RoundedCornerShape(12.dp)
    val shown = image
    Column(modifier = modifier.fillMaxWidth()) {
        when {
            loading -> Box(
                modifier = Modifier.fillMaxWidth().height(180.dp).clip(shape).background(ChipBlueTint),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NavyBlue, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
            }
            shown == null -> Box(
                modifier = Modifier.fillMaxWidth().clip(shape).background(ChipBlueTint).padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "The image couldn't be loaded. Check your internet connection and open the post again.",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateGray,
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                Image(
                    bitmap = shown,
                    contentDescription = "Announcement image. Tap to zoom.",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .border(1.dp, BorderGray, shape)
                        .clickable { zoomOpen = true }
                )
                Spacer(Modifier.height(4.dp))
                Text("Tap the image to zoom", style = MaterialTheme.typography.labelSmall, color = SlateGray)
                if (zoomOpen) ZoomDialog(image = shown, onClose = { zoomOpen = false })
            }
        }
    }
}

@Composable
private fun ZoomDialog(image: ImageBitmap, onClose: () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            Image(
                bitmap = image,
                contentDescription = "Zoomed image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            offset = if (scale == 1f) Offset.Zero else offset + pan
                        }
                    }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(8.dp)
            ) {
                Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}