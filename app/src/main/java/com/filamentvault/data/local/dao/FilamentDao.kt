package com.filamentvault.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.filamentvault.data.local.entity.FilamentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FilamentDao {

    @Query("SELECT * FROM filaments ORDER BY updated_at DESC")
    fun getAllFilaments(): Flow<List<FilamentEntity>>

    @Query("SELECT * FROM filaments WHERE id = :id")
    suspend fun getById(id: Long): FilamentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(filament: FilamentEntity): Long

    @Update
    suspend fun update(filament: FilamentEntity)

    @Delete
    suspend fun delete(filament: FilamentEntity)

    @Query("DELETE FROM filaments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        """
        SELECT * FROM filaments
        WHERE material_type = :materialType
          AND color_hex = :colorHex
          AND filament_diameter = :diameter
          AND (brand = :brand OR (brand IS NULL AND :brand IS NULL))
          AND (fill_type = :fillType OR (fill_type IS NULL AND :fillType IS NULL))
        LIMIT 1
        """
    )
    suspend fun findDuplicate(
        materialType: String,
        colorHex: String,
        diameter: Float,
        brand: String?,
        fillType: String?
    ): FilamentEntity?

    @Query("UPDATE filaments SET quantity = quantity + 1, updated_at = :now WHERE id = :id")
    suspend fun incrementQuantity(id: Long, now: Long = System.currentTimeMillis())

    @Query("SELECT DISTINCT brand FROM filaments WHERE brand IS NOT NULL ORDER BY brand")
    fun getAllBrands(): Flow<List<String>>

    @Query("SELECT DISTINCT material_type FROM filaments ORDER BY material_type")
    fun getAllMaterialTypes(): Flow<List<String>>
}
