package com.filamentvault.ui.screen.filamentdetail.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun ImageColorPickerDialog(
    imagePath: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val bitmap = remember(imagePath) {
        BitmapFactory.decodeFile(imagePath)
    }

    if (bitmap == null) {
        onDismiss()
        return
    }

    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
    val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

    var selectedColor by remember { mutableStateOf<Color?>(null) }
    var selectedHex by remember { mutableStateOf("") }
    var tapPosition by remember { mutableStateOf<Offset?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pick Color from Image") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Tap on the image to pick a color",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio.coerceIn(0.5f, 2f))
                ) {
                    Canvas(
                        modifier = Modifier
                            .matchParentSize()
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    // Map tap coordinates to bitmap pixel coordinates
                                    val canvasWidth = size.width.toFloat()
                                    val canvasHeight = size.height.toFloat()
                                    val px =
                                        (offset.x / canvasWidth * bitmap.width).toInt()
                                            .coerceIn(0, bitmap.width - 1)
                                    val py =
                                        (offset.y / canvasHeight * bitmap.height).toInt()
                                            .coerceIn(0, bitmap.height - 1)

                                    val pixel = bitmap.getPixel(px, py)
                                    val r = android.graphics.Color.red(pixel)
                                    val g = android.graphics.Color.green(pixel)
                                    val b = android.graphics.Color.blue(pixel)

                                    selectedColor = Color(r, g, b)
                                    selectedHex =
                                        String.format("#%02X%02X%02X", r, g, b)
                                    tapPosition = offset
                                }
                            }
                    ) {
                        // Draw the bitmap scaled to fill the canvas
                        drawImage(
                            image = imageBitmap,
                            srcOffset = IntOffset.Zero,
                            srcSize = IntSize(bitmap.width, bitmap.height),
                            dstOffset = IntOffset.Zero,
                            dstSize = IntSize(
                                size.width.toInt(),
                                size.height.toInt()
                            )
                        )

                        // Draw crosshair at tap position
                        tapPosition?.let { pos ->
                            // Outer ring (dark outline for visibility)
                            drawCircle(
                                color = Color.Black,
                                radius = 18f,
                                center = pos,
                                style = Stroke(width = 3f)
                            )
                            // Inner ring (white)
                            drawCircle(
                                color = Color.White,
                                radius = 18f,
                                center = pos,
                                style = Stroke(width = 1.5f)
                            )
                            // Center dot with selected color
                            selectedColor?.let { color ->
                                drawCircle(
                                    color = color,
                                    radius = 8f,
                                    center = pos
                                )
                            }
                        }
                    }
                }

                if (selectedColor != null) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(selectedColor!!)
                                .border(
                                    2.dp,
                                    MaterialTheme.colorScheme.outline,
                                    CircleShape
                                )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = selectedHex,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onColorSelected(selectedHex) },
                enabled = selectedHex.isNotBlank()
            ) {
                Text("Use This Color")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
