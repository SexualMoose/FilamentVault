package com.filamentvault.ui.screen.filamentdetail

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filamentvault.data.local.entity.DefaultSettingsEntity
import com.filamentvault.data.repository.FilamentRepository
import com.filamentvault.domain.model.Filament
import com.filamentvault.domain.usecase.AddFilamentUseCase
import com.filamentvault.domain.usecase.LookupDefaultsUseCase
import com.filamentvault.util.ImageStorageUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FilamentFormState(
    val id: Long = 0,
    val materialType: String = "",
    val colorHex: String = "#FF5733",
    val fillType: String = "Standard",
    val colorName: String = "",
    val brand: String = "",
    val filamentDiameter: Float = 1.75f,
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
    val dryingTemp: String = "",
    val dryingTime: String = "",
    val moistureSensitivity: String = "",
    val quantity: Int = 1,
    val notes: String = "",
    val imageUri: String? = null,
    val originalImageUri: String? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val defaultsApplied: Boolean = false,
    val availableBrands: List<String> = emptyList()
) {
    val isValid: Boolean
        get() = materialType.isNotBlank() && colorHex.isNotBlank()
}

data class DuplicateInfo(
    val existingId: Long,
    val currentQuantity: Int,
    val materialType: String,
    val brand: String?,
    val colorName: String?,
    val colorHex: String
)

sealed class DetailEvent {
    data object SaveSuccess : DetailEvent()
    data class DefaultsApplied(val materialType: String) : DetailEvent()
    data class Error(val message: String) : DetailEvent()
    data class DuplicateFound(val info: DuplicateInfo) : DetailEvent()
}

@HiltViewModel
class FilamentDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val addFilamentUseCase: AddFilamentUseCase,
    private val lookupDefaultsUseCase: LookupDefaultsUseCase,
    private val repository: FilamentRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(FilamentFormState())
    val formState: StateFlow<FilamentFormState> = _formState.asStateFlow()

    private val _events = MutableSharedFlow<DetailEvent>()
    val events: SharedFlow<DetailEvent> = _events.asSharedFlow()

    private val touchedFields = mutableSetOf<String>()
    private var defaultsLookupJob: Job? = null

    private val identityFields = setOf("materialType", "fillType", "brand")

    fun loadFilament(filamentId: Long?) {
        if (filamentId == null || filamentId <= 0) {
            loadBrands()
            return
        }
        viewModelScope.launch {
            val entity = repository.getFilamentById(filamentId) ?: return@launch
            val filament = Filament.fromEntity(entity)
            _formState.value = FilamentFormState(
                id = filament.id,
                materialType = filament.materialType,
                colorHex = filament.colorHex,
                fillType = filament.fillType ?: "Standard",
                colorName = filament.colorName ?: "",
                brand = filament.brand ?: "",
                filamentDiameter = filament.filamentDiameter,
                nozzleTempMin = filament.nozzleTempMin?.toString() ?: "",
                nozzleTempMax = filament.nozzleTempMax?.toString() ?: "",
                bedTempMin = filament.bedTempMin?.toString() ?: "",
                bedTempMax = filament.bedTempMax?.toString() ?: "",
                chamberTemp = filament.chamberTemp?.toString() ?: "",
                printSpeedMin = filament.printSpeedMin?.toString() ?: "",
                printSpeedMax = filament.printSpeedMax?.toString() ?: "",
                maxVolumetricSpeed = filament.maxVolumetricSpeed?.toString() ?: "",
                fanSpeedMin = filament.fanSpeedMin?.toString() ?: "",
                fanSpeedMax = filament.fanSpeedMax?.toString() ?: "",
                fanSpeedNotes = filament.fanSpeedNotes ?: "",
                retractionDistance = filament.retractionDistance?.toString() ?: "",
                retractionSpeed = filament.retractionSpeed?.toString() ?: "",
                flowRate = filament.flowRate?.toString() ?: "",
                layerHeightMin = filament.layerHeightMin?.toString() ?: "",
                layerHeightMax = filament.layerHeightMax?.toString() ?: "",
                density = filament.density?.toString() ?: "",
                dryingTemp = filament.dryingTemp?.toString() ?: "",
                dryingTime = filament.dryingTime?.toString() ?: "",
                moistureSensitivity = filament.moistureSensitivity ?: "",
                quantity = filament.quantity,
                notes = filament.notes ?: "",
                imageUri = filament.imageUri,
                originalImageUri = filament.originalImageUri ?: filament.imageUri,
                isEditing = true
            )
            loadBrands()
        }
    }

    private fun loadBrands() {
        viewModelScope.launch {
            val defaultBrands = repository.getAllDefaultBrands()
            // Also collect user-entered brands and merge with defaults
            repository.getAllBrands().collect { userBrands ->
                val merged = (defaultBrands + userBrands)
                    .distinct()
                    .sortedWith(String.CASE_INSENSITIVE_ORDER)
                _formState.update { it.copy(availableBrands = merged) }
            }
        }
    }

    fun updateField(field: String, value: String) {
        // Only mark non-identity fields as touched (identity fields drive defaults lookup)
        if (field !in identityFields) {
            touchedFields.add(field)
        }
        _formState.update { state ->
            when (field) {
                "materialType" -> state.copy(materialType = value)
                "colorHex" -> state.copy(colorHex = value)
                "fillType" -> state.copy(fillType = value)
                "colorName" -> state.copy(colorName = value)
                "brand" -> state.copy(brand = value)
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
                "notes" -> state.copy(notes = value)
                else -> state
            }
        }
        // Auto-trigger defaults re-lookup when any identity field changes
        if (field in identityFields) {
            scheduleLookupDefaults(debounceMs = if (field == "brand") 500L else 0L)
        }
    }

    fun updateDiameter(diameter: Float) {
        touchedFields.add("filamentDiameter")
        _formState.update { it.copy(filamentDiameter = diameter) }
    }

    fun updateColor(hex: String) {
        touchedFields.add("colorHex")
        _formState.update { it.copy(colorHex = hex) }
    }

    fun updateQuantity(delta: Int) {
        _formState.update { it.copy(quantity = (it.quantity + delta).coerceAtLeast(1)) }
    }

    private fun scheduleLookupDefaults(debounceMs: Long = 0L) {
        defaultsLookupJob?.cancel()
        defaultsLookupJob = viewModelScope.launch {
            if (debounceMs > 0) delay(debounceMs)
            val state = _formState.value
            if (state.materialType.isBlank()) return@launch

            val defaults = lookupDefaultsUseCase(
                materialType = state.materialType,
                brand = state.brand.ifBlank { null },
                fillType = state.fillType.ifBlank { null }
            ) ?: return@launch

            _formState.update { applyDefaults(it, defaults) }
            _events.emit(DetailEvent.DefaultsApplied(state.materialType))
        }
    }

    private fun applyDefaults(
        state: FilamentFormState,
        defaults: DefaultSettingsEntity
    ): FilamentFormState {
        return state.copy(
            nozzleTempMin = if ("nozzleTempMin" !in touchedFields)
                defaults.nozzleTempMin?.toString() ?: state.nozzleTempMin else state.nozzleTempMin,
            nozzleTempMax = if ("nozzleTempMax" !in touchedFields)
                defaults.nozzleTempMax?.toString() ?: state.nozzleTempMax else state.nozzleTempMax,
            bedTempMin = if ("bedTempMin" !in touchedFields)
                defaults.bedTempMin?.toString() ?: state.bedTempMin else state.bedTempMin,
            bedTempMax = if ("bedTempMax" !in touchedFields)
                defaults.bedTempMax?.toString() ?: state.bedTempMax else state.bedTempMax,
            chamberTemp = if ("chamberTemp" !in touchedFields)
                defaults.chamberTemp?.toString() ?: state.chamberTemp else state.chamberTemp,
            printSpeedMin = if ("printSpeedMin" !in touchedFields)
                defaults.printSpeedMin?.toString() ?: state.printSpeedMin else state.printSpeedMin,
            printSpeedMax = if ("printSpeedMax" !in touchedFields)
                defaults.printSpeedMax?.toString() ?: state.printSpeedMax else state.printSpeedMax,
            maxVolumetricSpeed = if ("maxVolumetricSpeed" !in touchedFields)
                defaults.maxVolumetricSpeed?.toString() ?: state.maxVolumetricSpeed else state.maxVolumetricSpeed,
            fanSpeedMin = if ("fanSpeedMin" !in touchedFields)
                defaults.fanSpeedMin?.toString() ?: state.fanSpeedMin else state.fanSpeedMin,
            fanSpeedMax = if ("fanSpeedMax" !in touchedFields)
                defaults.fanSpeedMax?.toString() ?: state.fanSpeedMax else state.fanSpeedMax,
            fanSpeedNotes = if ("fanSpeedNotes" !in touchedFields)
                defaults.fanSpeedNotes ?: state.fanSpeedNotes else state.fanSpeedNotes,
            retractionDistance = if ("retractionDistance" !in touchedFields)
                defaults.retractionDistance?.toString() ?: state.retractionDistance else state.retractionDistance,
            retractionSpeed = if ("retractionSpeed" !in touchedFields)
                defaults.retractionSpeed?.toString() ?: state.retractionSpeed else state.retractionSpeed,
            flowRate = if ("flowRate" !in touchedFields)
                defaults.flowRate?.toString() ?: state.flowRate else state.flowRate,
            layerHeightMin = if ("layerHeightMin" !in touchedFields)
                defaults.layerHeightMin?.toString() ?: state.layerHeightMin else state.layerHeightMin,
            layerHeightMax = if ("layerHeightMax" !in touchedFields)
                defaults.layerHeightMax?.toString() ?: state.layerHeightMax else state.layerHeightMax,
            density = if ("density" !in touchedFields)
                defaults.density?.toString() ?: state.density else state.density,
            dryingTemp = if ("dryingTemp" !in touchedFields)
                defaults.dryingTemp?.toString() ?: state.dryingTemp else state.dryingTemp,
            dryingTime = if ("dryingTime" !in touchedFields)
                defaults.dryingTime?.toString() ?: state.dryingTime else state.dryingTime,
            moistureSensitivity = if ("moistureSensitivity" !in touchedFields)
                defaults.moistureSensitivity ?: state.moistureSensitivity else state.moistureSensitivity,
            defaultsApplied = true
        )
    }

    /**
     * Called when the user picks a new photo (camera or gallery). We persist
     * the original to internal storage so it can be re-cropped later. The form
     * state's imageUri points to the same file until the cropper runs, which
     * replaces imageUri with the cropped spool thumbnail. The original is
     * preserved in originalImageUri.
     */
    fun setImageUri(uri: Uri) {
        viewModelScope.launch {
            val newOriginal = ImageStorageUtil.saveImage(context, uri) ?: return@launch

            // Clean up any prior original + cropped thumbnail
            val state = _formState.value
            state.originalImageUri?.let { ImageStorageUtil.deleteImage(it) }
            if (state.imageUri != null && state.imageUri != state.originalImageUri) {
                ImageStorageUtil.deleteImage(state.imageUri)
            }

            _formState.update {
                it.copy(imageUri = newOriginal, originalImageUri = newOriginal)
            }
        }
    }

    /**
     * Called after the spool cropper saves a new cropped thumbnail. The
     * original (if different) is kept on disk so Recrop can re-use it.
     */
    fun setImagePath(path: String) {
        val state = _formState.value
        val previous = state.imageUri
        // Delete the previous cropped thumbnail — but preserve the original
        if (previous != null && previous != path && previous != state.originalImageUri) {
            ImageStorageUtil.deleteImage(previous)
        }
        _formState.update { it.copy(imageUri = path) }
    }

    fun removeImage() {
        val state = _formState.value
        state.imageUri?.let { ImageStorageUtil.deleteImage(it) }
        if (state.originalImageUri != null && state.originalImageUri != state.imageUri) {
            ImageStorageUtil.deleteImage(state.originalImageUri)
        }
        _formState.update { it.copy(imageUri = null, originalImageUri = null) }
    }

    fun save() {
        val state = _formState.value
        if (!state.isValid) return

        _formState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                // Only check for duplicates when creating a new filament (not editing)
                if (!state.isEditing) {
                    val existing = repository.findDuplicate(
                        materialType = state.materialType,
                        colorHex = state.colorHex,
                        diameter = state.filamentDiameter,
                        brand = state.brand.ifBlank { null },
                        fillType = state.fillType.ifBlank { "Standard" }
                    )
                    if (existing != null) {
                        _formState.update { it.copy(isSaving = false) }
                        _events.emit(
                            DetailEvent.DuplicateFound(
                                DuplicateInfo(
                                    existingId = existing.id,
                                    currentQuantity = existing.quantity,
                                    materialType = existing.materialType,
                                    brand = existing.brand,
                                    colorName = existing.colorName,
                                    colorHex = existing.colorHex
                                )
                            )
                        )
                        return@launch
                    }
                }

                performSave(state)
            } catch (e: Exception) {
                _events.emit(DetailEvent.Error(e.message ?: "Failed to save"))
                _formState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun incrementExisting(existingId: Long) {
        viewModelScope.launch {
            try {
                repository.incrementQuantity(existingId)
                _events.emit(DetailEvent.SaveSuccess)
            } catch (e: Exception) {
                _events.emit(DetailEvent.Error(e.message ?: "Failed to update quantity"))
            }
        }
    }

    private suspend fun performSave(state: FilamentFormState) {
        try {
            val filament = Filament(
                id = state.id,
                materialType = state.materialType,
                colorHex = state.colorHex,
                fillType = state.fillType.ifBlank { "Standard" },
                colorName = state.colorName.ifBlank { null },
                brand = state.brand.ifBlank { null },
                filamentDiameter = state.filamentDiameter,
                nozzleTempMin = state.nozzleTempMin.toIntOrNull(),
                nozzleTempMax = state.nozzleTempMax.toIntOrNull(),
                bedTempMin = state.bedTempMin.toIntOrNull(),
                bedTempMax = state.bedTempMax.toIntOrNull(),
                chamberTemp = state.chamberTemp.toIntOrNull(),
                printSpeedMin = state.printSpeedMin.toIntOrNull(),
                printSpeedMax = state.printSpeedMax.toIntOrNull(),
                maxVolumetricSpeed = state.maxVolumetricSpeed.toFloatOrNull(),
                fanSpeedMin = state.fanSpeedMin.toIntOrNull(),
                fanSpeedMax = state.fanSpeedMax.toIntOrNull(),
                fanSpeedNotes = state.fanSpeedNotes.ifBlank { null },
                retractionDistance = state.retractionDistance.toFloatOrNull(),
                retractionSpeed = state.retractionSpeed.toFloatOrNull(),
                flowRate = state.flowRate.toFloatOrNull(),
                layerHeightMin = state.layerHeightMin.toFloatOrNull(),
                layerHeightMax = state.layerHeightMax.toFloatOrNull(),
                density = state.density.toFloatOrNull(),
                dryingTemp = state.dryingTemp.toIntOrNull(),
                dryingTime = state.dryingTime.toIntOrNull(),
                moistureSensitivity = state.moistureSensitivity.ifBlank { null },
                quantity = state.quantity,
                notes = state.notes.ifBlank { null },
                imageUri = state.imageUri,
                originalImageUri = state.originalImageUri
            )
            addFilamentUseCase(filament)
            _events.emit(DetailEvent.SaveSuccess)
        } catch (e: Exception) {
            _events.emit(DetailEvent.Error(e.message ?: "Failed to save"))
        } finally {
            _formState.update { it.copy(isSaving = false) }
        }
    }
}
