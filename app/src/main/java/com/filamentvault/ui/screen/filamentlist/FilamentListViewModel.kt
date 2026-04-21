package com.filamentvault.ui.screen.filamentlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filamentvault.data.repository.FilamentRepository
import com.filamentvault.domain.model.Filament
import com.filamentvault.domain.model.FilterCriteria
import com.filamentvault.domain.usecase.DeleteFilamentUseCase
import com.filamentvault.domain.usecase.GetFilamentsUseCase
import com.filamentvault.util.ColorWheelSort
import com.filamentvault.util.ImageStorageUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FilamentListUiState(
    val filaments: List<Filament> = emptyList(),
    val isLoading: Boolean = true,
    val filterCriteria: FilterCriteria = FilterCriteria(),
    val availableBrands: List<String> = emptyList()
)

@HiltViewModel
class FilamentListViewModel @Inject constructor(
    private val getFilamentsUseCase: GetFilamentsUseCase,
    private val deleteFilamentUseCase: DeleteFilamentUseCase,
    private val repository: FilamentRepository
) : ViewModel() {

    private val _filterCriteria = MutableStateFlow(FilterCriteria())
    val filterCriteria: StateFlow<FilterCriteria> = _filterCriteria.asStateFlow()

    val uiState: StateFlow<FilamentListUiState> = combine(
        getFilamentsUseCase(),
        _filterCriteria,
        repository.getAllBrands()
    ) { filaments, criteria, brands ->
        val filtered = if (criteria.isActive) {
            filaments.filter { criteria.matches(it) }
        } else {
            filaments
        }
        // Always sort in traditional color-wheel order
        val sorted = ColorWheelSort.sort(filtered)
        FilamentListUiState(
            filaments = sorted,
            isLoading = false,
            filterCriteria = criteria,
            availableBrands = brands
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FilamentListUiState()
    )

    fun updateFilter(criteria: FilterCriteria) {
        _filterCriteria.value = criteria
    }

    fun clearFilters() {
        _filterCriteria.value = FilterCriteria()
    }

    fun deleteFilament(filament: Filament) {
        viewModelScope.launch {
            ImageStorageUtil.deleteImage(filament.imageUri)
            if (filament.originalImageUri != null &&
                filament.originalImageUri != filament.imageUri) {
                ImageStorageUtil.deleteImage(filament.originalImageUri)
            }
            deleteFilamentUseCase(filament.id)
        }
    }
}
