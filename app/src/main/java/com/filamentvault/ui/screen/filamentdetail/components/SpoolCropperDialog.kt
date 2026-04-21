package com.filamentvault.ui.screen.filamentdetail.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.exifinterface.media.ExifInterface
import coil3.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream

/**
 * Fullscreen overlay that lets the user pan + pinch-zoom + rotate an image
 * behind a vertical spool-shaped silhouette. Renders inside the parent
 * Activity window (not as a Dialog) so edge-to-edge insets work correctly.
 * On confirm, the pixels inside the mask are saved as a new JPEG.
 */
@Composable
fun SpoolCropperDialog(
    sourcePath: String,
    onCropped: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Gesture state
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var rotation by remember { mutableFloatStateOf(0f) }

    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var isSaving by remember { mutableStateOf(false) }

    // Fullscreen overlay — sits on top of the parent screen and consumes gestures
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.98f))
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Position the filament inside the spool outline",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Drag to move  \u00B7  Pinch to zoom  \u00B7  Use buttons to rotate",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )

            // Cropping stage fills all remaining vertical space above the buttons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black)
                    .onSizeChanged { viewportSize = it }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.3f, 6f)
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                    }
            ) {
                // Image — panned / zoomed / rotated around its center
                AsyncImage(
                    model = File(sourcePath),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offsetX
                            translationY = offsetY
                            rotationZ = rotation
                        }
                )

                // Spool silhouette overlay — dims everything outside the capsule shape
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val maskInset = w * 0.12f
                    val maskRx = (w - 2 * maskInset) / 2f
                    val maskLeft = maskInset
                    val maskRight = w - maskInset
                    val maskTop = h * 0.05f
                    val maskBottom = h * 0.95f

                    val outer = Path().apply {
                        addRect(androidx.compose.ui.geometry.Rect(0f, 0f, w, h))
                    }
                    val inner = Path().apply {
                        addRoundRect(
                            androidx.compose.ui.geometry.RoundRect(
                                left = maskLeft,
                                top = maskTop,
                                right = maskRight,
                                bottom = maskBottom,
                                radiusX = maskRx,
                                radiusY = maskRx
                            )
                        )
                    }
                    val dimRegion = Path().apply {
                        op(outer, inner, PathOperation.Difference)
                    }
                    drawPath(dimRegion, color = Color.Black.copy(alpha = 0.78f))
                    drawPath(
                        inner,
                        color = Color.White.copy(alpha = 0.9f),
                        style = Stroke(width = 3f)
                    )
                }
            }

            // Rotation controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { rotation -= 90f },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.RotateLeft, contentDescription = "Rotate left")
                }
                IconButton(
                    onClick = { rotation += 90f },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Rotate right")
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = {
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                        rotation = 0f
                    }
                ) {
                    Text("Reset", color = Color.White.copy(alpha = 0.8f))
                }
            }

            // Action buttons — always visible, pinned to the bottom of the column
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", color = Color.White)
                }
                Button(
                    onClick = {
                        if (!isSaving && viewportSize.width > 0) {
                            isSaving = true
                            cropSpoolAsync(
                                context = context,
                                sourcePath = sourcePath,
                                viewportW = viewportSize.width,
                                viewportH = viewportSize.height,
                                scale = scale,
                                offsetX = offsetX,
                                offsetY = offsetY,
                                rotation = rotation,
                                onDone = { newPath ->
                                    isSaving = false
                                    if (newPath != null) onCropped(newPath)
                                    else onDismiss()
                                }
                            )
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isSaving) "Saving..." else "Use photo")
                }
            }
        }
    }
}

// Kicks off the async crop on a worker thread
private fun cropSpoolAsync(
    context: android.content.Context,
    sourcePath: String,
    viewportW: Int,
    viewportH: Int,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    rotation: Float,
    onDone: (String?) -> Unit
) {
    val main = android.os.Handler(android.os.Looper.getMainLooper())
    Thread {
        val result = try {
            cropSpool(
                context, sourcePath,
                viewportW, viewportH,
                scale, offsetX, offsetY, rotation
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        main.post { onDone(result) }
    }.start()
}

/**
 * Reads the EXIF orientation of a JPEG and returns the degrees a viewer
 * would rotate it to display "upright". BitmapFactory does NOT apply this
 * automatically, so we must do it manually to match what Coil/AsyncImage shows.
 */
private fun readExifRotation(path: String): Float {
    return try {
        val exif = ExifInterface(path)
        when (exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    } catch (e: Exception) {
        0f
    }
}

private fun cropSpool(
    context: android.content.Context,
    sourcePath: String,
    viewportW: Int,
    viewportH: Int,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    rotation: Float
): String? {
    val raw = BitmapFactory.decodeFile(sourcePath) ?: return null

    // Apply EXIF orientation so the source bitmap matches what's shown on screen
    val exifDegrees = readExifRotation(sourcePath)
    val source = if (exifDegrees != 0f) {
        val m = Matrix().apply { postRotate(exifDegrees) }
        val rotated = Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, m, true)
        if (rotated != raw) raw.recycle()
        rotated
    } else raw

    // Base "ContentScale.Fit" transform — scale down so the bitmap fits the viewport
    val baseScale = minOf(
        viewportW.toFloat() / source.width,
        viewportH.toFloat() / source.height
    )
    val drawnW = source.width * baseScale
    val drawnH = source.height * baseScale
    val baseX = (viewportW - drawnW) / 2f
    val baseY = (viewportH - drawnH) / 2f

    val out = Bitmap.createBitmap(viewportW, viewportH, Bitmap.Config.ARGB_8888)
    val c = AndroidCanvas(out)

    val cx = viewportW / 2f
    val cy = viewportH / 2f

    val m = Matrix()
    // 1. Fit inside viewport, centered
    m.postScale(baseScale, baseScale, 0f, 0f)
    m.postTranslate(baseX, baseY)
    // 2. Apply user zoom around viewport center
    m.postTranslate(-cx, -cy)
    m.postScale(scale, scale, 0f, 0f)
    // 3. Apply user rotation around viewport center
    m.postRotate(rotation, 0f, 0f)
    m.postTranslate(cx, cy)
    // 4. Apply user pan
    m.postTranslate(offsetX, offsetY)

    c.drawBitmap(source, m, Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))

    // Mask to the capsule shape (everything outside → transparent)
    val maskInset = viewportW * 0.12f
    val maskRx = (viewportW - 2 * maskInset) / 2f
    val maskRect = RectF(
        maskInset,
        viewportH * 0.05f,
        viewportW - maskInset,
        viewportH * 0.95f
    )

    val maskBitmap = Bitmap.createBitmap(viewportW, viewportH, Bitmap.Config.ARGB_8888)
    val maskCanvas = AndroidCanvas(maskBitmap)
    val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE }
    maskCanvas.drawRoundRect(maskRect, maskRx, maskRx, maskPaint)

    val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }
    c.drawBitmap(maskBitmap, 0f, 0f, clearPaint)

    // Trim to the mask's bounding box
    val cropLeft = maskRect.left.toInt().coerceAtLeast(0)
    val cropTop = maskRect.top.toInt().coerceAtLeast(0)
    val cropRight = maskRect.right.toInt().coerceAtMost(viewportW)
    val cropBottom = maskRect.bottom.toInt().coerceAtMost(viewportH)
    val cropW = cropRight - cropLeft
    val cropH = cropBottom - cropTop

    val cropped = Bitmap.createBitmap(out, cropLeft, cropTop, cropW, cropH)

    // JPEG has no alpha — flatten onto a neutral dark background
    val flat = Bitmap.createBitmap(cropped.width, cropped.height, Bitmap.Config.ARGB_8888)
    val fc = AndroidCanvas(flat)
    fc.drawColor(android.graphics.Color.rgb(18, 18, 18))
    fc.drawBitmap(cropped, 0f, 0f, null)

    val dir = File(context.filesDir, "filament_images").apply { mkdirs() }
    val file = File(dir, "spool_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { fos ->
        flat.compress(Bitmap.CompressFormat.JPEG, 90, fos)
    }

    source.recycle()
    maskBitmap.recycle()
    out.recycle()
    cropped.recycle()
    flat.recycle()

    return file.absolutePath
}
