package com.filamentvault.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.filamentvault.data.local.entity.DefaultSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DefaultSettingsDao {

    @Query("SELECT * FROM default_settings ORDER BY material_type, brand, fill_type")
    fun getAllFlow(): Flow<List<DefaultSettingsEntity>>

    @Query("SELECT * FROM default_settings")
    suspend fun getAll(): List<DefaultSettingsEntity>

    @Query("SELECT * FROM default_settings WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DefaultSettingsEntity?

    @Query(
        """
        SELECT * FROM default_settings
        WHERE material_type = :materialType
          AND brand = :brand
          AND fill_type = :fillType
        ORDER BY id DESC LIMIT 1
        """
    )
    suspend fun findExact(
        materialType: String,
        brand: String,
        fillType: String
    ): DefaultSettingsEntity?

    @Query(
        """
        SELECT * FROM default_settings
        WHERE material_type = :materialType
          AND brand = :brand
          AND fill_type IS NULL
        ORDER BY id DESC LIMIT 1
        """
    )
    suspend fun findByMaterialAndBrand(
        materialType: String,
        brand: String
    ): DefaultSettingsEntity?

    @Query(
        """
        SELECT * FROM default_settings
        WHERE material_type = :materialType
          AND brand IS NULL
          AND fill_type = :fillType
        ORDER BY id DESC LIMIT 1
        """
    )
    suspend fun findByMaterialAndFillType(
        materialType: String,
        fillType: String
    ): DefaultSettingsEntity?

    @Query(
        """
        SELECT * FROM default_settings
        WHERE material_type = :materialType
          AND brand IS NULL
          AND fill_type IS NULL
        ORDER BY id DESC LIMIT 1
        """
    )
    suspend fun findByMaterial(materialType: String): DefaultSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(defaults: List<DefaultSettingsEntity>)

    @Query("DELETE FROM default_settings")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM default_settings")
    suspend fun getCount(): Int

    @Query("SELECT DISTINCT brand FROM default_settings WHERE brand IS NOT NULL ORDER BY brand")
    suspend fun getAllBrands(): List<String>
}
