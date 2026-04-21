package com.filamentvault.ui.screen.filamentlist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.filamentvault.domain.model.Filament
import com.filamentvault.ui.common.ColorSwatchCircle
import java.io.File

@Composable
fun FilamentCard(
    filament: Filament,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showThumbnail: Boolean = false
) {
    // Fixed card height so every card is the same size — and so an image
    // thumbnail can't blow the card up to its intrinsic pixel dimensions.
    val cardHeight = 108.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 14.dp)
            ) {
                ColorSwatchCircle(
                    colorHex = filament.colorHex,
                    size = 80.dp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 14.dp, horizontal = 0.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = filament.materialType,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val subtitle = buildList {
                    filament.brand?.let { add(it) }
                    filament.fillType?.takeIf { it.isNotBlank() && it != "Standard" }?.let { add(it) }
                }.joinToString(" \u00B7 ")

                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    filament.colorName?.let { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Qty: ${filament.quantity}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (filament.quantity > 1)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val tempInfo = buildList {
                    if (filament.nozzleTempMin != null || filament.nozzleTempMax != null) {
                        val nozzle = formatTempRange(filament.nozzleTempMin, filament.nozzleTempMax)
                        add("Nozzle: $nozzle")
                    }
                    if (filament.bedTempMin != null || filament.bedTempMax != null) {
                        val bed = formatTempRange(filament.bedTempMin, filament.bedTempMax)
                        add("Bed: $bed")
                    }
                }.joinToString("  \u00B7  ")

                if (tempInfo.isNotBlank()) {
                    Text(
                        text = tempInfo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }

            if (showThumbnail) {
                Spacer(modifier = Modifier.width(12.dp))
                ThumbnailBox(
                    imageUri = filament.imageUri,
                    colorHex = filament.colorHex,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                )
            } else {
                // Trailing padding when no thumbnail
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
}

@Composable
private fun ThumbnailBox(
    imageUri: String?,
    colorHex: String,
    modifier: Modifier = Modifier
) {
    val swatchColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color.Gray
    }

    // Rounded only on the leading (left) edge so the trailing edge flushes with
    // the card's right side, giving an edge-to-edge top-to-bottom thumbnail.
    val shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .background(swatchColor)
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = File(imageUri),
                contentDescription = "Filament photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

private fun formatTempRange(min: Int?, max: Int?): String {
    return when {
        min != null && max != null && min != max -> "${min}-${max}\u00B0C"
        min != null -> "${min}\u00B0C"
        max != null -> "${max}\u00B0C"
        else -> ""
    }
}
