package com.filamentvault.ui.screen.defaultsdb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filamentvault.data.local.entity.DefaultOverrideEntity
import com.filamentvault.data.local.entity.DefaultSettingsEntity
import com.filamentvault.data.repository.FilamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * One row in the defaults list UI. Represents either:
 *  - A built-in default (possibly with an override applied) — `baseId` set, `overrideId` may be set
 *  - A user-added custom default — `baseId == null`, `overrideId` set
 */
data class DefaultEntryView(
    val baseId: Long?,
    val overrideId: Long?,
    val materialType: String,
    val brand: String?,
    val fillType: String?,
    val nozzleTempMin: Int?,
    val nozzleTempMax: Int?,
    val bedTempMin: Int?,
    val bedTempMax: Int?,
    val isModified: Boolean,
    val isCustom: Boolean
)

data class DefaultsListUiState(
    val entries: List<DefaultEntryView> = emptyList(),
    val isLoading: Boolean = true,
    val query: String = "",
    val totalCount: Int = 0,
    val modifiedCount: Int = 0,
    val customCount: Int = 0
)

@HiltViewModel
class DefaultsListViewModel @Inject constructor(
    private val repository: FilamentRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val uiState: StateFlow<DefaultsListUiState> = combine(
        repository.getAllDefaultsFlow(),
        repository.getAllOverridesFlow(),
        _query
    ) { defaults, overrides, query ->
        buildState(defaults, overrides, query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DefaultsListUiState()
    )

    fun setQuery(q: String) {
        _query.value = q
    }

    fun resetOverride(baseId: Long) {
        viewModelScope.launch {
            repository.deleteOverrideByBaseId(baseId)
        }
    }

    fun deleteCustom(overrideId: Long) {
        viewModelScope.launch {
            repository.deleteOverride(overrideId)
        }
    }

    private fun buildState(
        defaults: List<DefaultSettingsEntity>,
        overrides: List<DefaultOverrideEntity>,
        query: String
    ): DefaultsListUiState {
        val overridesByBaseId = overrides.filter { it.baseId != null }.associateBy { it.baseId!! }
        val customOverrides = overrides.filter { it.baseId == null }

        val baseEntries = defaults.map { d ->
            val ovr = overridesByBaseId[d.id]
            val effective = ovr?.toDefaultEntryView() ?: d.toDefaultEntryView()
            effective.copy(
                baseId = d.id,
                overrideId = ovr?.id,
                isModified = ovr != null,
                isCustom = false
            )
        }

        val customEntries = customOverrides.map { ovr ->
            ovr.toDefaultEntryView().copy(
                baseId = null,
                overrideId = ovr.id,
                isModified = false,
                isCustom = true
            )
        }

        val all = (customEntries + baseEntries)
            .sortedWith(
                compareBy<DefaultEntryView> { !it.isCustom }       // customs first
                    .thenBy { it.materialType }
                    .thenBy { it.brand ?: "" }
                    .thenBy { it.fillType ?: "" }
            )

        val filtered = if (query.isBlank()) all else {
            val q = query.trim().lowercase()
            all.filter {
                it.materialType.lowercase().contains(q) ||
                    (it.brand?.lowercase()?.contains(q) == true) ||
                    (it.fillType?.lowercase()?.contains(q) == true)
            }
        }

        return DefaultsListUiState(
            entries = filtered,
            isLoading = false,
            query = query,
            totalCount = all.size,
            modifiedCount = all.count { it.isModified },
            customCount = all.count { it.isCustom }
        )
    }
}

private fun DefaultSettingsEntity.toDefaultEntryView() = DefaultEntryView(
    baseId = id,
    overrideId = null,
    materialType = materialType,
    brand = brand,
    fillType = fillType,
    nozzleTempMin = nozzleTempMin,
    nozzleTempMax = nozzleTempMax,
    bedTempMin = bedTempMin,
    bedTempMax = bedTempMax,
    isModified = false,
    isCustom = false
)

private fun DefaultOverrideEntity.toDefaultEntryView() = DefaultEntryView(
    baseId = baseId,
    overrideId = id,
    materialType = materialType,
    brand = brand,
    fillType = fillType,
    nozzleTempMin = nozzleTempMin,
    nozzleTempMax = nozzleTempMax,
    bedTempMin = bedTempMin,
    bedTempMax = bedTempMax,
    isModified = false,
    isCustom = false
)
