package com.filamentvault.util

import android.content.Context
import android.net.Uri
import com.filamentvault.data.local.entity.DefaultOverrideEntity
import com.filamentvault.data.local.entity.FilamentEntity
import com.filamentvault.data.repository.FilamentRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Full-data backup bundle — all user-owned state:
 *  - inventory (FilamentEntity rows)
 *  - database customizations (DefaultOverrideEntity rows)
 *
 * Built-in default_settings are NOT included — they come bundled with the app.
 */
@Serializable
data class BackupBundle(
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val filaments: List<FilamentDto>,
    val defaultOverrides: List<DefaultOverrideDto>
)

@Serializable
data class FilamentDto(
    val materialType: String,
    val colorHex: String,
    val fillType: String? = null,
    val colorName: String? = null,
    val brand: String? = null,
    val filamentDiameter: Float = 1.75f,
    val nozzleTempMin: Int? = null,
    val nozzleTempMax: Int? = null,
    val bedTempMin: Int? = null,
    val bedTempMax: Int? = null,
    val chamberTemp: Int? = null,
    val printSpeedMin: Int? = null,
    val printSpeedMax: Int? = null,
    val maxVolumetricSpeed: Float? = null,
    val fanSpeedMin: Int? = null,
    val fanSpeedMax: Int? = null,
    val fanSpeedNotes: String? = null,
    val retractionDistance: Float? = null,
    val retractionSpeed: Float? = null,
    val flowRate: Float? = null,
    val layerHeightMin: Float? = null,
    val layerHeightMax: Float? = null,
    val density: Float? = null,
    val dryingTemp: Int? = null,
    val dryingTime: Int? = null,
    val moistureSensitivity: String? = null,
    val quantity: Int = 1,
    val notes: String? = null,
    val originalImageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class DefaultOverrideDto(
    val baseMaterial: String?,
    val baseBrand: String?,
    val baseFillType: String?,
    val materialType: String,
    val brand: String? = null,
    val fillType: String? = null,
    val nozzleTempMin: Int? = null,
    val nozzleTempMax: Int? = null,
    val bedTempMin: Int? = null,
    val bedTempMax: Int? = null,
    val chamberTemp: Int? = null,
    val printSpeedMin: Int? = null,
    val printSpeedMax: Int? = null,
    val maxVolumetricSpeed: Float? = null,
    val fanSpeedMin: Int? = null,
    val fanSpeedMax: Int? = null,
    val fanSpeedNotes: String? = null,
    val retractionDistance: Float? = null,
    val retractionSpeed: Float? = null,
    val flowRate: Float? = null,
    val layerHeightMin: Float? = null,
    val layerHeightMax: Float? = null,
    val density: Float? = null,
    val filamentDiameter: Float = 1.75f,
    val dryingTemp: Int? = null,
    val dryingTime: Int? = null,
    val moistureSensitivity: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

sealed class RestoreResult {
    data class Success(val filamentCount: Int, val overrideCount: Int) : RestoreResult()
    data class Failure(val message: String) : RestoreResult()
}

/**
 * Serializes and writes the backup to the given URI (selected via SAF).
 * The URI can point to local storage, Google Drive, Downloads, OneDrive, etc.
 */
object BackupUtil {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    suspend fun export(
        context: Context,
        uri: Uri,
        repository: FilamentRepository
    ): Result<Int> {
        return try {
            val filaments = repository.getAllFilaments().first().map { it.toDto() }
            val overrides = buildOverrideDtos(repository)
            val bundle = BackupBundle(
                filaments = filaments,
                defaultOverrides = overrides
            )
            val text = json.encodeToString(BackupBundle.serializer(), bundle)
            context.contentResolver.openOutputStream(uri, "w")?.use { out ->
                out.write(text.toByteArray(Charsets.UTF_8))
                out.flush()
            } ?: return Result.failure(IllegalStateException("Could not open output stream"))
            Result.success(filaments.size + overrides.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun import(
        context: Context,
        uri: Uri,
        repository: FilamentRepository,
        replaceExisting: Boolean
    ): RestoreResult {
        return try {
            val text = context.contentResolver.openInputStream(uri)?.use { input ->
                input.readBytes().toString(Charsets.UTF_8)
            } ?: return RestoreResult.Failure("Could not read file")

            val bundle = json.decodeFromString(BackupBundle.serializer(), text)

            // Restore filaments
            if (replaceExisting) {
                val existing = repository.getAllFilaments().first()
                for (e in existing) {
                    repository.deleteFilament(e.id)
                    ImageStorageUtil.deleteImage(e.imageUri)
                    if (e.originalImageUri != null && e.originalImageUri != e.imageUri) {
                        ImageStorageUtil.deleteImage(e.originalImageUri)
                    }
                }
            }
            for (dto in bundle.filaments) {
                repository.insertFilament(dto.toEntity())
            }

            // Restore overrides — resolve baseId by looking up material/brand/fillType
            if (replaceExisting) {
                repository.clearAllOverrides()
            }
            val defaults = repository.getAllDefaults()
            fun matchBaseId(mat: String?, brand: String?, fill: String?): Long? {
                if (mat == null) return null
                return defaults.firstOrNull {
                    it.materialType == mat &&
                        it.brand == brand &&
                        it.fillType == fill
                }?.id
            }

            for (dto in bundle.defaultOverrides) {
                val baseId = matchBaseId(dto.baseMaterial, dto.baseBrand, dto.baseFillType)
                repository.saveOverride(dto.toEntity(baseId))
            }

            RestoreResult.Success(bundle.filaments.size, bundle.defaultOverrides.size)
        } catch (e: Exception) {
            RestoreResult.Failure(e.message ?: "Import failed")
        }
    }

    private suspend fun buildOverrideDtos(repo: FilamentRepository): List<DefaultOverrideDto> {
        val overrides = repo.getAllOverrides()
        val defaults = repo.getAllDefaults().associateBy { it.id }
        return overrides.map { ovr ->
            val base = ovr.baseId?.let { defaults[it] }
            DefaultOverrideDto(
                baseMaterial = base?.materialType,
                baseBrand = base?.brand,
                baseFillType = base?.fillType,
                materialType = ovr.materialType,
                brand = ovr.brand,
                fillType = ovr.fillType,
                nozzleTempMin = ovr.nozzleTempMin,
                nozzleTempMax = ovr.nozzleTempMax,
                bedTempMin = ovr.bedTempMin,
                bedTempMax = ovr.bedTempMax,
                chamberTemp = ovr.chamberTemp,
                printSpeedMin = ovr.printSpeedMin,
                printSpeedMax = ovr.printSpeedMax,
                maxVolumetricSpeed = ovr.maxVolumetricSpeed,
                fanSpeedMin = ovr.fanSpeedMin,
                fanSpeedMax = ovr.fanSpeedMax,
                fanSpeedNotes = ovr.fanSpeedNotes,
                retractionDistance = ovr.retractionDistance,
                retractionSpeed = ovr.retractionSpeed,
                flowRate = ovr.flowRate,
                layerHeightMin = ovr.layerHeightMin,
                layerHeightMax = ovr.layerHeightMax,
                density = ovr.density,
                filamentDiameter = ovr.filamentDiameter,
                dryingTemp = ovr.dryingTemp,
                dryingTime = ovr.dryingTime,
                moistureSensitivity = ovr.moistureSensitivity,
                updatedAt = ovr.updatedAt
            )
        }
    }
}

// Entity <-> DTO mapping
private fun FilamentEntity.toDto() = FilamentDto(
    materialType = materialType,
    colorHex = colorHex,
    fillType = fillType,
    colorName = colorName,
    brand = brand,
    filamentDiameter = filamentDiameter,
    nozzleTempMin = nozzleTempMin,
    nozzleTempMax = nozzleTempMax,
    bedTempMin = bedTempMin,
    bedTempMax = bedTempMax,
    chamberTemp = chamberTemp,
    printSpeedMin = printSpeedMin,
    printSpeedMax = printSpeedMax,
    maxVolumetricSpeed = maxVolumetricSpeed,
    fanSpeedMin = fanSpeedMin,
    fanSpeedMax = fanSpeedMax,
    fanSpeedNotes = fanSpeedNotes,
    retractionDistance = retractionDistance,
    retractionSpeed = retractionSpeed,
    flowRate = flowRate,
    layerHeightMin = layerHeightMin,
    layerHeightMax = layerHeightMax,
    density = density,
    dryingTemp = dryingTemp,
    dryingTime = dryingTime,
    moistureSensitivity = moistureSensitivity,
    quantity = quantity,
    notes = notes,
    originalImageUri = originalImageUri,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun FilamentDto.toEntity() = FilamentEntity(
    materialType = materialType,
    colorHex = colorHex,
    fillType = fillType,
    colorName = colorName,
    brand = brand,
    filamentDiameter = filamentDiameter,
    nozzleTempMin = nozzleTempMin,
    nozzleTempMax = nozzleTempMax,
    bedTempMin = bedTempMin,
    bedTempMax = bedTempMax,
    chamberTemp = chamberTemp,
    printSpeedMin = printSpeedMin,
    printSpeedMax = printSpeedMax,
    maxVolumetricSpeed = maxVolumetricSpeed,
    fanSpeedMin = fanSpeedMin,
    fanSpeedMax = fanSpeedMax,
    fanSpeedNotes = fanSpeedNotes,
    retractionDistance = retractionDistance,
    retractionSpeed = retractionSpeed,
    flowRate = flowRate,
    layerHeightMin = layerHeightMin,
    layerHeightMax = layerHeightMax,
    density = density,
    dryingTemp = dryingTemp,
    dryingTime = dryingTime,
    moistureSensitivity = moistureSensitivity,
    quantity = quantity,
    notes = notes,
    originalImageUri = originalImageUri,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun DefaultOverrideDto.toEntity(baseId: Long?) = DefaultOverrideEntity(
    baseId = baseId,
    materialType = materialType,
    brand = brand,
    fillType = fillType,
    nozzleTempMin = nozzleTempMin,
    nozzleTempMax = nozzleTempMax,
    bedTempMin = bedTempMin,
    bedTempMax = bedTempMax,
    chamberTemp = chamberTemp,
    printSpeedMin = printSpeedMin,
    printSpeedMax = printSpeedMax,
    maxVolumetricSpeed = maxVolumetricSpeed,
    fanSpeedMin = fanSpeedMin,
    fanSpeedMax = fanSpeedMax,
    fanSpeedNotes = fanSpeedNotes,
    retractionDistance = retractionDistance,
    retractionSpeed = retractionSpeed,
    flowRate = flowRate,
    layerHeightMin = layerHeightMin,
    layerHeightMax = layerHeightMax,
    density = density,
    filamentDiameter = filamentDiameter,
    dryingTemp = dryingTemp,
    dryingTime = dryingTime,
    moistureSensitivity = moistureSensitivity,
    updatedAt = updatedAt
)
