package com.filamentvault.data.repository

import com.filamentvault.data.local.entity.DefaultOverrideEntity
import com.filamentvault.data.local.entity.DefaultSettingsEntity
import com.filamentvault.data.local.entity.FilamentEntity
import kotlinx.coroutines.flow.Flow

interface FilamentRepository {
    fun getAllFilaments(): Flow<List<FilamentEntity>>
    suspend fun getFilamentById(id: Long): FilamentEntity?
    suspend fun insertFilament(filament: FilamentEntity): Long
    suspend fun updateFilament(filament: FilamentEntity)
    suspend fun deleteFilament(id: Long)
    suspend fun findDuplicate(
        materialType: String,
        colorHex: String,
        diameter: Float,
        brand: String?,
        fillType: String?
    ): FilamentEntity?
    suspend fun incrementQuantity(id: Long)
    fun getAllBrands(): Flow<List<String>>

    suspend fun lookupDefaults(
        materialType: String,
        brand: String?,
        fillType: String?
    ): DefaultSettingsEntity?

    suspend fun getAllDefaultBrands(): List<String>

    // Default catalog + overrides
    fun getAllDefaultsFlow(): Flow<List<DefaultSettingsEntity>>
    suspend fun getAllDefaults(): List<DefaultSettingsEntity>
    suspend fun getDefaultById(id: Long): DefaultSettingsEntity?
    fun getAllOverridesFlow(): Flow<List<DefaultOverrideEntity>>
    suspend fun getAllOverrides(): List<DefaultOverrideEntity>
    suspend fun getOverrideByBaseId(baseId: Long): DefaultOverrideEntity?
    suspend fun getOverrideById(id: Long): DefaultOverrideEntity?
    suspend fun saveOverride(override: DefaultOverrideEntity): Long
    suspend fun deleteOverride(id: Long)
    suspend fun deleteOverrideByBaseId(baseId: Long)
    suspend fun clearAllOverrides()
}
