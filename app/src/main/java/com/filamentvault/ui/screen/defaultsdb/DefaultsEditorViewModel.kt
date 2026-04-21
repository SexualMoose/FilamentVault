package com.filamentvault.ui.screen.defaultsdb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filamentvault.data.local.entity.DefaultOverrideEntity
import com.filamentvault.data.local.entity.DefaultSettingsEntity
import com.filamentvault.data.repository.FilamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DefaultsEditorState(
    val baseId: Long? = null,
    val overrideId: Long? = null,
    val materialType: String = "",
    val brand: String = "",
    val fillType: String = "Standard",
    val nozzleTempMin: String = "",
    val nozzleTempMax: String = "",
    val bedTempMin: String = "",
    val bedTempMax: String = "",
    val chamberTemp: String = "",
    val printSpeedMin: String = "",
    val printSpeedMax: String = "",
    val maxVolumetricSpeed: String = "",
    val fanSpeedMin: String = "",
    val fanSpeedMax: String = "",
    val fanSpeedNotes: String = "",
    val retractionDistance: String = "",
    val retractionSpeed: String = "",
    val flowRate: String = "",
    val layerHeightMin: String = "",
    val layerHeightMax: String = "",
    val density: String = "",
    val filamentDiameter: Float = 1.75f,
    val dryingTemp: String = "",
    val dryingTime: String = "",
    val moistureSensitivity: String = "",
    val isBuiltIn: Boolean = false,
    val isModified: Boolean = false,
    val isCustom: Boolean = false,
    val isLoading: Boolean = false,
    val availableBrands: List<String> = emptyList()
) {
    val canSave: Boolean
        get() = materialType.isNotBlank()
}

sealed class EditorEvent {
    data object SaveSuccess : EditorEvent()
    data object ResetSuccess : EditorEvent()
    data class Error(val message: String) : EditorEvent()
    data class DuplicateKey(val message: String) : EditorEvent()
    data class Message(val text: String) : EditorEvent()
}

@HiltViewModel
class DefaultsEditorViewModel @Inject constructor(
    private val repository: FilamentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DefaultsEditorState())
    val state: StateFlow<DefaultsEditorState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<EditorEvent>()
    val events: SharedFlow<EditorEvent> = _events.asSharedFlow()

    fun load(baseId: Long?, overrideId: Long?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val brands = repository.getAllDefaultBrands()
            val base = baseId?.let { repository.getDefaultById(it) }
            val override = overrideId?.let { repository.getOverrideById(it) }
                ?: base?.id?.let { repository.getOverrideByBaseId(it) }

            val source: DefaultsEditorState = when {
                override != null -> override.toEditorState(baseId = override.baseId, overrideId = override.id)
                base != null -> base.toEditorState(baseId = base.id, overrideId = null)
                else -> DefaultsEditorState()
            }
            _state.value = source.copy(
                availableBrands = brands,
                isBuiltIn = base != null && override == null,
                isModified = base != null && override != null,
                isCustom = base == null && override != null,
                isLoading = false
            )
        }
    }

    fun update(field: String, value: String) {
        _state.update { state ->
            when (field) {
                "materialType" -> state.copy(materialType = value)
                "brand" -> state.copy(brand = value)
                "fillType" -> state.copy(fillType = value)
                "nozzleTempMin" -> state.copy(nozzleTempMin = value)
                "nozzleTempMax" -> state.copy(nozzleTempMax = value)
                "bedTempMin" -> state.copy(bedTempMin = value)
                "bedTempMax" -> state.copy(bedTempMax = value)
                "chamberTemp" -> state.copy(chamberTemp = value)
                "printSpeedMin" -> state.copy(printSpeedMin = value)
                "printSpeedMax" -> state.copy(printSpeedMax = value)
                "maxVolumetricSpeed" -> state.copy(maxVolumetricSpeed = value)
                "fanSpeedMin" -> state.copy(fanSpeedMin = value)
                "fanSpeedMax" -> state.copy(fanSpeedMax = value)
                "fanSpeedNotes" -> state.copy(fanSpeedNotes = value)
                "retractionDistance" -> state.copy(retractionDistance = value)
                "retractionSpeed" -> state.copy(retractionSpeed = value)
                "flowRate" -> state.copy(flowRate = value)
                "layerHeightMin" -> state.copy(layerHeightMin = value)
                "layerHeightMax" -> state.copy(layerHeightMax = value)
                "density" -> state.copy(density = value)
                "dryingTemp" -> state.copy(dryingTemp = value)
                "dryingTime" -> state.copy(dryingTime = value)
                "moistureSensitivity" -> state.copy(moistureSensitivity = value)
                else -> state
            }
        }
    }

    fun updateDiameter(d: Float) {
        _state.update { it.copy(filamentDiameter = d) }
    }

    /**
     * Overwrite the built-in default with the current form values.
     * The original is preserved in default_settings; the edit lives in default_overrides.
     */
    fun saveOverBase() {
        val s = _state.value
        if (!s.canSave || s.baseId == null) return
        viewModelScope.launch {
            try {
                val existing = repository.getOverrideByBaseId(s.baseId)
                val override = s.toOverride(existingId = existing?.id ?: 0, baseId = s.baseId)
                repository.saveOverride(override)
                _events.emit(EditorEvent.SaveSuccess)
            } catch (e: Exception) {
                _events.emit(errorFor(e))
            }
        }
    }

    /**
     * Save as a new custom entry (baseId = null). This is how the user creates
     * brand-new defaults that were not in the built-in catalog.
     */
    fun saveAsNew() {
        val s = _state.value
        if (!s.canSave) return
        viewModelScope.launch {
            try {
                val override = s.toOverride(existingId = 0, baseId = null)
                repository.saveOverride(override)
                _events.emit(EditorEvent.SaveSuccess)
            } catch (e: Exception) {
                _events.emit(errorFor(e))
            }
        }
    }

    /**
     * Update the current custom entry (baseId already null) in place.
     */
    fun saveCustom() {
        val s = _state.value
        val oid = s.overrideId ?: return
        if (!s.canSave) return
        viewModelScope.launch {
            try {
                val override = s.toOverride(existingId = oid, baseId = null)
                repository.saveOverride(override)
                _events.emit(EditorEvent.SaveSuccess)
            } catch (e: Exception) {
                _events.emit(errorFor(e))
            }
        }
    }

    fun resetToOriginal() {
        val s = _state.value
        val baseId = s.baseId ?: return
        viewModelScope.launch {
            try {
                repository.deleteOverrideByBaseId(baseId)
                _events.emit(EditorEvent.ResetSuccess)
            } catch (e: Exception) {
                _events.emit(EditorEvent.Error(e.message ?: "Failed to reset"))
            }
        }
    }

    /**
     * Reset this single entry back to its built-in values:
     *  - deletes any saved override for this baseId (no-op if none)
     *  - reloads the form with the pristine built-in values so the user
     *    can see them and continue editing from there
     * Unlike [resetToOriginal], this does NOT navigate away.
     */
    fun resetFieldsToBuiltIn() {
        val baseId = _state.value.baseId ?: return
        viewModelScope.launch {
            try {
                repository.deleteOverrideByBaseId(baseId)
                load(baseId, null)
                _events.emit(EditorEvent.Message("Reset to built-in values"))
            } catch (e: Exception) {
                _events.emit(EditorEvent.Error(e.message ?: "Reset failed"))
            }
        }
    }

    fun deleteCustom() {
        val oid = _state.value.overrideId ?: return
        viewModelScope.launch {
            try {
                repository.deleteOverride(oid)
                _events.emit(EditorEvent.ResetSuccess)
            } catch (e: Exception) {
                _events.emit(EditorEvent.Error(e.message ?: "Failed to delete"))
            }
        }
    }

    private fun errorFor(e: Exception): EditorEvent {
        val msg = e.message ?: "Save failed"
        return if (msg.contains("UNIQUE", ignoreCase = true))
            EditorEvent.DuplicateKey("A default with that Material + Brand + Modifier already exists.")
        else EditorEvent.Error(msg)
    }
}

private fun DefaultSettingsEntity.toEditorState(baseId: Long?, overrideId: Long?): DefaultsEditorState =
    DefaultsEditorState(
        baseId = baseId,
        overrideId = overrideId,
        materialType = materialType,
        brand = brand ?: "",
        fillType = fillType ?: "Standard",
        nozzleTempMin = nozzleTempMin?.toString() ?: "",
        nozzleTempMax = nozzleTempMax?.toString() ?: "",
        bedTempMin = bedTempMin?.toString() ?: "",
        bedTempMax = bedTempMax?.toString() ?: "",
        chamberTemp = chamberTemp?.toString() ?: "",
        printSpeedMin = printSpeedMin?.toString() ?: "",
        printSpeedMax = printSpeedMax?.toString() ?: "",
        maxVolumetricSpeed = maxVolumetricSpeed?.toString() ?: "",
        fanSpeedMin = fanSpeedMin?.toString() ?: "",
        fanSpeedMax = fanSpeedMax?.toString() ?: "",
        fanSpeedNotes = fanSpeedNotes ?: "",
        retractionDistance = retractionDistance?.toString() ?: "",
        retractionSpeed = retractionSpeed?.toString() ?: "",
        flowRate = flowRate?.toString() ?: "",
        layerHeightMin = layerHeightMin?.toString() ?: "",
        layerHeightMax = layerHeightMax?.toString() ?: "",
        density = density?.toString() ?: "",
        filamentDiameter = filamentDiameter,
        dryingTemp = dryingTemp?.toString() ?: "",
        dryingTime = dryingTime?.toString() ?: "",
        moistureSensitivity = moistureSensitivity ?: ""
    )

private fun DefaultOverrideEntity.toEditorState(baseId: Long?, overrideId: Long?): DefaultsEditorState =
    DefaultsEditorState(
        baseId = baseId,
        overrideId = overrideId,
        materialType = materialType,
        brand = brand ?: "",
        fillType = fillType ?: "Standard",
        nozzleTempMin = nozzleTempMin?.toString() ?: "",
        nozzleTempMax = nozzleTempMax?.toString() ?: "",
        bedTempMin = bedTempMin?.toString() ?: "",
        bedTempMax = bedTempMax?.toString() ?: "",
        chamberTemp = chamberTemp?.toString() ?: "",
        printSpeedMin = printSpeedMin?.toString() ?: "",
        printSpeedMax = printSpeedMax?.toString() ?: "",
        maxVolumetricSpeed = maxVolumetricSpeed?.toString() ?: "",
        fanSpeedMin = fanSpeedMin?.toString() ?: "",
        fanSpeedMax = fanSpeedMax?.toString() ?: "",
        fanSpeedNotes = fanSpeedNotes ?: "",
        retractionDistance = retractionDistance?.toString() ?: "",
        retractionSpeed = retractionSpeed?.toString() ?: "",
        flowRate = flowRate?.toString() ?: "",
        layerHeightMin = layerHeightMin?.toString() ?: "",
        layerHeightMax = layerHeightMax?.toString() ?: "",
        density = density?.toString() ?: "",
        filamentDiameter = filamentDiameter,
        dryingTemp = dryingTemp?.toString() ?: "",
        dryingTime = dryingTime?.toString() ?: "",
        moistureSensitivity = moistureSensitivity ?: ""
    )

private fun DefaultsEditorState.toOverride(existingId: Long, baseId: Long?): DefaultOverrideEntity =
    DefaultOverrideEntity(
        id = existingId,
        baseId = baseId,
        materialType = materialType.trim(),
        brand = brand.ifBlank { null },
        fillType = fillType.ifBlank { "Standard" },
        nozzleTempMin = nozzleTempMin.toIntOrNull(),
        nozzleTempMax = nozzleTempMax.toIntOrNull(),
        bedTempMin = bedTempMin.toIntOrNull(),
        bedTempMax = bedTempMax.toIntOrNull(),
        chamberTemp = chamberTemp.toIntOrNull(),
        printSpeedMin = printSpeedMin.toIntOrNull(),
        printSpeedMax = printSpeedMax.toIntOrNull(),
        maxVolumetricSpeed = maxVolumetricSpeed.toFloatOrNull(),
        fanSpeedMin = fanSpeedMin.toIntOrNull(),
        fanSpeedMax = fanSpeedMax.toIntOrNull(),
        fanSpeedNotes = fanSpeedNotes.ifBlank { null },
        retractionDistance = retractionDistance.toFloatOrNull(),
        retractionSpeed = retractionSpeed.toFloatOrNull(),
        flowRate = flowRate.toFloatOrNull(),
        layerHeightMin = layerHeightMin.toFloatOrNull(),
        layerHeightMax = layerHeightMax.toFloatOrNull(),
        density = density.toFloatOrNull(),
        filamentDiameter = filamentDiameter,
        dryingTemp = dryingTemp.toIntOrNull(),
        dryingTime = dryingTime.toIntOrNull(),
        moistureSensitivity = moistureSensitivity.ifBlank { null },
        updatedAt = System.currentTimeMillis()
    )
