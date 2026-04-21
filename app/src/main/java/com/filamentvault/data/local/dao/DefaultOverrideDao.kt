package com.filamentvault.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.filamentvault.data.local.entity.DefaultOverrideEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DefaultOverrideDao {

    @Query("SELECT * FROM default_overrides")
    fun getAllFlow(): Flow<List<DefaultOverrideEntity>>

    @Query("SELECT * FROM default_overrides")
    suspend fun getAll(): List<DefaultOverrideEntity>

    @Query("SELECT * FROM default_overrides WHERE base_id = :baseId LIMIT 1")
    suspend fun findByBaseId(baseId: Long): DefaultOverrideEntity?

    @Query(
        """
        SELECT * FROM default_overrides
        WHERE material_type = :materialType
          AND (brand = :brand OR (brand IS NULL AND :brand IS NULL))
          AND (fill_type = :fillType OR (fill_type IS NULL AND :fillType IS NULL))
        LIMIT 1
        """
    )
    suspend fun findByKey(
        materialType: String,
        brand: String?,
        fillType: String?
    ): DefaultOverrideEntity?

    @Query("SELECT * FROM default_overrides WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DefaultOverrideEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(override: DefaultOverrideEntity): Long

    @Update
    suspend fun update(override: DefaultOverrideEntity)

    @Query("DELETE FROM default_overrides WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM default_overrides WHERE base_id = :baseId")
    suspend fun deleteByBaseId(baseId: Long)

    @Query("DELETE FROM default_overrides")
    suspend fun deleteAll()
}
