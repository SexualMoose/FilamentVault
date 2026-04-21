package com.filamentvault.domain.usecase

import com.filamentvault.domain.model.Filament
import com.filamentvault.data.repository.FilamentRepository
import javax.inject.Inject

class AddFilamentUseCase @Inject constructor(
    private val repository: FilamentRepository
) {
    suspend operator fun invoke(filament: Filament): Long {
        val now = System.currentTimeMillis()
        val entity = filament.toEntity().copy(
            createdAt = if (filament.id == 0L) now else filament.createdAt,
            updatedAt = now
        )
        return if (filament.id == 0L) {
            repository.insertFilament(entity)
        } else {
            repository.updateFilament(entity)
            entity.id
        }
    }
}
