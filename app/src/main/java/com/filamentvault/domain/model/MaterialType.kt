package com.filamentvault.domain.model

enum class MaterialType(val displayName: String) {
    PLA("PLA"),
    ABS("ABS"),
    PETG("PETG"),
    TPU("TPU"),
    NYLON("Nylon"),
    ASA("ASA"),
    PC("PC"),
    PCTG("PCTG"),
    HIPS("HIPS"),
    PVA("PVA"),
    PP("PP"),
    PEI("PEI/ULTEM"),
    CF_NYLON("CF Nylon"),
    CF_PETG("CF PETG"),
    PEEK("PEEK"),
    POM("POM");

    companion object {
        fun fromString(value: String): MaterialType? =
            entries.find {
                it.name.equals(value, ignoreCase = true) ||
                    it.displayName.equals(value, ignoreCase = true)
            }

        val allDisplayNames: List<String> = entries.map { it.displayName }
    }
}
