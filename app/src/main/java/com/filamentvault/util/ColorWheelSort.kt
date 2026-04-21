package com.filamentvault.util

import android.graphics.Color as AndroidColor
import com.filamentvault.domain.model.Filament

/**
 * Sort filaments in traditional color-wheel order:
 * red → orange → yellow → green → cyan → blue → magenta → back to red.
 *
 * Low-saturation colors (grays/blacks/whites) are grouped separately at the end,
 * sorted by lightness (black → gray → white).
 */
object ColorWheelSort {

    private data class HsvKey(
        val isGray: Boolean,
        val hue: Float,
        val lightness: Float,
        val saturation: Float
    )

    private fun hsvKey(colorHex: String): HsvKey {
        val argb = try {
            AndroidColor.parseColor(colorHex)
        } catch (e: Exception) {
            AndroidColor.GRAY
        }
        val hsv = FloatArray(3)
        AndroidColor.colorToHSV(argb, hsv)
        val hue = hsv[0]            // 0..360
        val saturation = hsv[1]     // 0..1
        val value = hsv[2]          // 0..1

        // Treat near-grayscale as grays so they cluster together
        val isGray = saturation < 0.12f

        return HsvKey(
            isGray = isGray,
            hue = hue,
            lightness = value,
            saturation = saturation
        )
    }

    fun sort(filaments: List<Filament>): List<Filament> {
        return filaments.sortedWith(
            compareBy<Filament> { hsvKey(it.colorHex).isGray }
                .thenBy { hsvKey(it.colorHex).hue }
                .thenBy { -hsvKey(it.colorHex).saturation }
                .thenBy { hsvKey(it.colorHex).lightness }
        )
    }
}
