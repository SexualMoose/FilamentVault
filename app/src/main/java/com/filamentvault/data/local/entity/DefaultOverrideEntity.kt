package com.filamentvault.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * User-edited or user-created default setting.
 *
 * - When `baseId` is non-null, this row is a user edit of a built-in default from
 *   default_settings. The original is preserved in default_settings; this row wins
 *   on lookup and can be deleted to "Reset to original".
 * - When `baseId` is null, this row is a user-created new default (not in the
 *   built-in catalog). "Delete" removes it entirely.
 */
@Entity(
    tableName = "default_overrides",
    indices = [
        Index(value = ["base_id"], unique = false),
        Index(value = ["material_type", "brand", "fill_type"], unique = true)
    ]
)
data class DefaultOverrideEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "base_id")
    val baseId: Long? = null,

    @ColumnInfo(name = "material_type")
    val materialType: String,

    @ColumnInfo(name = "brand")
    val brand: String? = null,

    @ColumnInfo(name = "fill_type")
    val fillType: String? = null,

    @ColumnInfo(name = "nozzle_temp_min")
    val nozzleTempMin: Int? = null,

    @ColumnInfo(name = "nozzle_temp_max")
    val nozzleTempMax: Int? = null,

    @ColumnInfo(name = "bed_temp_min")
    val bedTempMin: Int? = null,

    @ColumnInfo(name = "bed_temp_max")
    val bedTempMax: Int? = null,

    @ColumnInfo(name = "chamber_temp")
    val chamberTemp: Int? = null,

    @ColumnInfo(name = "print_speed_min")
    val printSpeedMin: Int? = null,

    @ColumnInfo(name = "print_speed_max")
    val printSpeedMax: Int? = null,

    @ColumnInfo(name = "max_volumetric_speed")
    val maxVolumetricSpeed: Float? = null,

    @ColumnInfo(name = "fan_speed_min")
    val fanSpeedMin: Int? = null,

    @ColumnInfo(name = "fan_speed_max")
    val fanSpeedMax: Int? = null,

    @ColumnInfo(name = "fan_speed_notes")
    val fanSpeedNotes: String? = null,

    @ColumnInfo(name = "retraction_distance")
    val retractionDistance: Float? = null,

    @ColumnInfo(name = "retraction_speed")
    val retractionSpeed: Float? = null,

    @ColumnInfo(name = "flow_rate")
    val flowRate: Float? = null,

    @ColumnInfo(name = "layer_height_min")
    val layerHeightMin: Float? = null,

    @ColumnInfo(name = "layer_height_max")
    val layerHeightMax: Float? = null,

    @ColumnInfo(name = "density")
    val density: Float? = null,

    @ColumnInfo(name = "filament_diameter")
    val filamentDiameter: Float = 1.75f,

    @ColumnInfo(name = "drying_temp")
    val dryingTemp: Int? = null,

    @ColumnInfo(name = "drying_time")
    val dryingTime: Int? = null,

    @ColumnInfo(name = "moisture_sensitivity")
    val moistureSensitivity: String? = null,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
