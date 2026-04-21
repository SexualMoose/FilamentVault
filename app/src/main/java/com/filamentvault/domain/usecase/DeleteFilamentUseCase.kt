package com.filamentvault.domain.usecase

import com.filamentvault.data.repository.FilamentRepository
import javax.inject.Inject

class DeleteFilamentUseCase @Inject constructor(
    private val repository: FilamentRepository
) {
    suspend operator fun invoke(filamentId: Long) {
        repository.deleteFilament(filamentId)
    }
}
