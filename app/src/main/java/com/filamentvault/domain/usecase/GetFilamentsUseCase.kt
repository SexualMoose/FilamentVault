package com.filamentvault.domain.usecase

import com.filamentvault.data.repository.FilamentRepository
import com.filamentvault.domain.model.Filament
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetFilamentsUseCase @Inject constructor(
    private val repository: FilamentRepository
) {
    operator fun invoke(): Flow<List<Filament>> {
        return repository.getAllFilaments().map { entities ->
            entities.map { Filament.fromEntity(it) }
        }
    }
}
