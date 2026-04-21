package com.filamentvault.domain.usecase

import com.filamentvault.data.local.entity.DefaultSettingsEntity
import com.filamentvault.data.repository.FilamentRepository
import javax.inject.Inject

class LookupDefaultsUseCase @Inject constructor(
    private val repository: FilamentRepository
) {
    suspend operator fun invoke(
        materialType: String,
        brand: String?,
        fillType: String?
    ): DefaultSettingsEntity? {
        return repository.lookupDefaults(materialType, brand, fillType)
    }
}
