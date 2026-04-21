package com.filamentvault.domain.model

data class FilterCriteria(
    val materialTypes: Set<String> = emptySet(),
    val fillTypes: Set<String> = emptySet(),
    val brands: Set<String> = emptySet(),
    val nozzleTempMin: Int? = null,
    val nozzleTempMax: Int? = null,
    val bedTempMin: Int? = null,
    val bedTempMax: Int? = null,
    val chamberTempMin: Int? = null,
    val filamentDiameter: Float? = null,
    val moistureSensitivities: Set<String> = emptySet(),
    val searchQuery: String = ""
) {
    val isActive: Boolean
        get() = materialTypes.isNotEmpty() ||
            fillTypes.isNotEmpty() ||
            brands.isNotEmpty() ||
            nozzleTempMin != null ||
            nozzleTempMax != null ||
            bedTempMin != null ||
            bedTempMax != null ||
            chamberTempMin != null ||
            filamentDiameter != null ||
            moistureSensitivities.isNotEmpty() ||
            searchQuery.isNotBlank()

    val activeFilterCount: Int
        get() {
            var count = 0
            if (materialTypes.isNotEmpty()) count++
            if (fillTypes.isNotEmpty()) count++
            if (brands.isNotEmpty()) count++
            if (nozzleTempMin != null || nozzleTempMax != null) count++
            if (bedTempMin != null || bedTempMax != null) count++
            if (chamberTempMin != null) count++
            if (filamentDiameter != null) count++
            if (moistureSensitivities.isNotEmpty()) count++
            if (searchQuery.isNotBlank()) count++
            return count
        }

    fun matches(filament: Filament): Boolean {
        if (materialTypes.isNotEmpty() &&
            filament.materialType !in materialTypes
        ) return false

        if (fillTypes.isNotEmpty() &&
            (filament.fillType == null || filament.fillType !in fillTypes)
        ) return false

        if (brands.isNotEmpty() &&
            (filament.brand == null || filament.brand !in brands)
        ) return false

        if (nozzleTempMin != null &&
            (filament.nozzleTempMax == null || filament.nozzleTempMax < nozzleTempMin)
        ) return false

        if (nozzleTempMax != null &&
            (filament.nozzleTempMin == null || filament.nozzleTempMin > nozzleTempMax)
        ) return false

        if (bedTempMin != null &&
            (filament.bedTempMax == null || filament.bedTempMax < bedTempMin)
        ) return false

        if (bedTempMax != null &&
            (filament.bedTempMin == null || filament.bedTempMin > bedTempMax)
        ) return false

        if (chamberTempMin != null &&
            (filament.chamberTemp == null || filament.chamberTemp < chamberTempMin)
        ) return false

        if (filamentDiameter != null &&
            filament.filamentDiameter != filamentDiameter
        ) return false

        if (moistureSensitivities.isNotEmpty() &&
            (filament.moistureSensitivity == null ||
                filament.moistureSensitivity !in moistureSensitivities)
        ) return false

        if (searchQuery.isNotBlank()) {
            val query = searchQuery.lowercase()
            val searchable = listOfNotNull(
                filament.materialType,
                filament.fillType,
                filament.brand,
                filament.colorName
            ).joinToString(" ").lowercase()
            if (query !in searchable) return false
        }

        return true
    }
}
