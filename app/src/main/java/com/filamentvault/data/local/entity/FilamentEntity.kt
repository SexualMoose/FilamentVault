package com.filamentvault.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "filaments")
data class FilamentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Required fields
    @ColumnInfo(name = "material_type")
    val materialType: String,

    @ColumnInfo(name = "color_hex")
    val colorHex: String,

    // Optional identity
    @ColumnInfo(name = "fill_type")
    val fillType: String? = null,

    @ColumnInfo(name = "color_name")
    val colorName: String? = null,

    @ColumnInfo(name = "brand")
    val brand: String? = null,

    @ColumnInfo(name = "filament_diameter")
    val filamentDiameter: Float = 1.75f,

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

    // Drying / moisture
    @ColumnInfo(name = "drying_temp")
    val dryingTemp: Int? = null,

    @ColumnInfo(name = "drying_time")
    val dryingTime: Int? = null,

    @ColumnInfo(name = "moisture_sensitivity")
    val moistureSensitivity: String? = null,

    // Inventory
    @ColumnInfo(name = "quantity", defaultValue = "1")
    val quantity: Int = 1,

    // User content
    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "image_uri")
    val imageUri: String? = null,

    @ColumnInfo(name = "original_image_uri")
    val originalImageUri: String? = null,

    // Metadata
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
