package com.filamentvault.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.filamentvault.data.local.converter.Converters
import com.filamentvault.data.local.dao.DefaultOverrideDao
import com.filamentvault.data.local.dao.DefaultSettingsDao
import com.filamentvault.data.local.dao.FilamentDao
import com.filamentvault.data.local.entity.DefaultOverrideEntity
import com.filamentvault.data.local.entity.DefaultSettingsEntity
import com.filamentvault.data.local.entity.FilamentEntity

@Database(
    entities = [
        FilamentEntity::class,
        DefaultSettingsEntity::class,
        DefaultOverrideEntity::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FilamentVaultDatabase : RoomDatabase() {
    abstract fun filamentDao(): FilamentDao
    abstract fun defaultSettingsDao(): DefaultSettingsDao
    abstract fun defaultOverrideDao(): DefaultOverrideDao

    companion object {
        /**
         * Adds the default_overrides table introduced in v4.
         * Non-destructive: user filaments and default_settings are preserved.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `default_overrides` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `base_id` INTEGER,
                        `material_type` TEXT NOT NULL,
                        `brand` TEXT,
                        `fill_type` TEXT,
                        `nozzle_temp_min` INTEGER,
                        `nozzle_temp_max` INTEGER,
                        `bed_temp_min` INTEGER,
                        `bed_temp_max` INTEGER,
                        `chamber_temp` INTEGER,
                        `print_speed_min` INTEGER,
                        `print_speed_max` INTEGER,
                        `max_volumetric_speed` REAL,
                        `fan_speed_min` INTEGER,
                        `fan_speed_max` INTEGER,
                        `fan_speed_notes` TEXT,
                        `retraction_distance` REAL,
                        `retraction_speed` REAL,
                        `flow_rate` REAL,
                        `layer_height_min` REAL,
                        `layer_height_max` REAL,
                        `density` REAL,
                        `filament_diameter` REAL NOT NULL DEFAULT 1.75,
                        `drying_temp` INTEGER,
                        `drying_time` INTEGER,
                        `moisture_sensitivity` TEXT,
                        `updated_at` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_default_overrides_base_id` ON `default_overrides` (`base_id`)")
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_default_overrides_material_type_brand_fill_type` " +
                        "ON `default_overrides` (`material_type`, `brand`, `fill_type`)"
                )
            }
        }

        /**
         * Adds original_image_uri to filaments so the user can re-crop back to
         * the source photo (imageUri holds the cropped spool thumbnail).
         * Non-destructive: existing rows get NULL for the new column.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `filaments` ADD COLUMN `original_image_uri` TEXT")
            }
        }
    }
}
