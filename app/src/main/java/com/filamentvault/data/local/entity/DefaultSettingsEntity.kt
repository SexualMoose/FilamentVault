package com.filamentvault.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "default_settings",
    indices = [
        Index(value = ["material_type", "brand", "fill_type"], unique = true)
    ]
)
data class DefaultSettingsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Lookup keys — null means wildcard
    @ColumnInfo(name = "material_type")
    val materialType: String,

    @ColumnInfo(name = "brand")
    val brand: String? = null,

    @ColumnInfo(name = "fill_type")
    val fillType: String? = null,

    // Temperature settings
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

    // Speed settings
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

    // Retraction settings
    @ColumnInfo(name = "retraction_distance")
    val retractionDistance: Float? = null,

    @ColumnInfo(name = "retraction_speed")
    val retractionSpeed: Float? = null,

    // Extrusion settings
    @ColumnInfo(name = "flow_rate")
    val flowRate: Float? = null,

    @ColumnInfo(name = "layer_height_min")
    val layerHeightMin: Float? = null,

    @ColumnInfo(name = "layer_height_max")
    val layerHeightMax: Float? = null,

    // Physical properties
    @ColumnInfo(name = "density")
    val density: Float? = null,

    @ColumnInfo(name = "filament_diameter")
    val filamentDiameter: Float = 1.75f,

    // Drying / moisture
    @ColumnInfo(name = "drying_temp")
    val dryingTemp: Int? = null,

    @ColumnInfo(name = "drying_time")
    val dryingTime: Int? = null,

    @ColumnInfo(name = "moisture_sensitivity")
    val moistureSensitivity: String? = null
)
