package com.filamentvault.data.repository

import com.filamentvault.data.local.dao.DefaultOverrideDao
import com.filamentvault.data.local.dao.DefaultSettingsDao
import com.filamentvault.data.local.dao.FilamentDao
import com.filamentvault.data.local.entity.DefaultOverrideEntity
import com.filamentvault.data.local.entity.DefaultSettingsEntity
import com.filamentvault.data.local.entity.FilamentEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilamentRepositoryImpl @Inject constructor(
    private val filamentDao: FilamentDao,
    private val defaultSettingsDao: DefaultSettingsDao,
    private val defaultOverrideDao: DefaultOverrideDao
) : FilamentRepository {

    override fun getAllFilaments(): Flow<List<FilamentEntity>> =
        filamentDao.getAllFilaments()

    override suspend fun getFilamentById(id: Long): FilamentEntity? =
        filamentDao.getById(id)

    override suspend fun insertFilament(filament: FilamentEntity): Long =
        filamentDao.insert(filament)

    override suspend fun updateFilament(filament: FilamentEntity) =
        filamentDao.update(filament)

    override suspend fun deleteFilament(id: Long) =
        filamentDao.deleteById(id)

    override suspend fun findDuplicate(
        materialType: String,
        colorHex: String,
        diameter: Float,
        brand: String?,
        fillType: String?
    ): FilamentEntity? =
        filamentDao.findDuplicate(materialType, colorHex, diameter, brand, fillType)

    override suspend fun incrementQuantity(id: Long) =
        filamentDao.incrementQuantity(id)

    override fun getAllBrands(): Flow<List<String>> =
        filamentDao.getAllBrands()

    override suspend fun lookupDefaults(
        materialType: String,
        brand: String?,
        fillType: String?
    ): DefaultSettingsEntity? {
        // 1. Check user overrides first — exact, then partial cascade
        defaultOverrideDao.findByKey(materialType, brand, fillType)?.let {
            return it.toDefaultSettingsEntity()
        }
        if (brand != null && fillType != null) {
            defaultOverrideDao.findByKey(materialType, brand, null)?.let {
                return it.toDefaultSettingsEntity()
            }
        }
        if (brand != null) {
            defaultOverrideDao.findByKey(materialType, null, fillType)?.let {
                return it.toDefaultSettingsEntity()
            }
        }
        defaultOverrideDao.findByKey(materialType, null, null)?.let {
            return it.toDefaultSettingsEntity()
        }

        // 2. Fall back to built-in catalog
        if (brand != null && fillType != null) {
            defaultSettingsDao.findExact(materialType, brand, fillType)?.let { return it }
        }
        if (brand != null) {
            defaultSettingsDao.findByMaterialAndBrand(materialType, brand)?.let { return it }
        }
        if (fillType != null) {
            defaultSettingsDao.findByMaterialAndFillType(materialType, fillType)?.let { return it }
        }
        return defaultSettingsDao.findByMaterial(materialType)
    }

    override suspend fun getAllDefaultBrands(): List<String> {
        val baseBrands = defaultSettingsDao.getAllBrands()
        val overrideBrands = defaultOverrideDao.getAll()
            .mapNotNull { it.brand }
            .distinct()
        return (baseBrands + overrideBrands).distinct().sortedWith(String.CASE_INSENSITIVE_ORDER)
    }

    override fun getAllDefaultsFlow(): Flow<List<DefaultSettingsEntity>> =
        defaultSettingsDao.getAllFlow()

    override suspend fun getAllDefaults(): List<DefaultSettingsEntity> =
        defaultSettingsDao.getAll()

    override suspend fun getDefaultById(id: Long): DefaultSettingsEntity? =
        defaultSettingsDao.getById(id)

    override fun getAllOverridesFlow(): Flow<List<DefaultOverrideEntity>> =
        defaultOverrideDao.getAllFlow()

    override suspend fun getAllOverrides(): List<DefaultOverrideEntity> =
        defaultOverrideDao.getAll()

    override suspend fun getOverrideByBaseId(baseId: Long): DefaultOverrideEntity? =
        defaultOverrideDao.findByBaseId(baseId)

    override suspend fun getOverrideById(id: Long): DefaultOverrideEntity? =
        defaultOverrideDao.getById(id)

    override suspend fun saveOverride(override: DefaultOverrideEntity): Long =
        defaultOverrideDao.insert(override)

    override suspend fun deleteOverride(id: Long) =
        defaultOverrideDao.deleteById(id)

    override suspend fun deleteOverrideByBaseId(baseId: Long) =
        defaultOverrideDao.deleteByBaseId(baseId)

    override suspend fun clearAllOverrides() =
        defaultOverrideDao.deleteAll()

    private fun DefaultOverrideEntity.toDefaultSettingsEntity() = DefaultSettingsEntity(
        id = id,
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
        moistureSensitivity = moistureSensitivity
    )
}
