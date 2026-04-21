package com.filamentvault.util

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.filamentvault.data.local.dao.DefaultSettingsDao
import com.filamentvault.data.local.entity.DefaultSettingsEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Provider

@Serializable
data class DefaultSettingsJson(
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
    val moistureSensitivity: String? = null
)

@Serializable
data class DefaultsWrapper(val defaults: List<DefaultSettingsJson>)

class DefaultsPopulator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val defaultSettingsDaoProvider: Provider<DefaultSettingsDao>
) : RoomDatabase.Callback() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        scope.launch {
            val dao = defaultSettingsDaoProvider.get()
            val currentCount = dao.getCount()
            val entities = loadFromAssets()
            // Re-seed if empty or if the asset has a different number of entries
            // (indicates the catalog was updated in a new app version)
            if (currentCount == 0 || currentCount != entities.size) {
                dao.clearAll()
                dao.insertAll(entities)
            }
        }
    }

    private fun loadFromAssets(): List<DefaultSettingsEntity> {
        val json = Json { ignoreUnknownKeys = true }
        val jsonString = context.assets.open("defaults.json")
            .bufferedReader()
            .use { it.readText() }
        val wrapper = json.decodeFromString<DefaultsWrapper>(jsonString)
        return wrapper.defaults.map { it.toEntity() }
    }

    private fun DefaultSettingsJson.toEntity() = DefaultSettingsEntity(
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
