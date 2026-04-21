package com.filamentvault.domain.model

/**
 * Material modifier — represents the "flavor" of a base material.
 * UI label is "Modifier"; class name kept as FillType to avoid collision with Compose's Modifier.
 */
enum class FillType(val displayName: String) {
    STANDARD("Standard"),
    PLUS("Plus (+)"),
    PRO("Pro"),
    HIGH_SPEED("High Speed (HS)"),
    TOUGH("Tough/Impact"),
    AIR("Air/LW (Foaming)"),
    CRYSTAL("Crystal/Clear"),
    SILK("Silk"),
    MATTE("Matte"),
    MARBLE("Marble"),
    GRADIENT("Gradient/Multicolor"),
    GLOW_IN_DARK("Glow-in-Dark"),
    CARBON_FIBER("Carbon Fiber"),
    GLASS_FIBER("Glass Fiber"),
    WOOD_FILL("Wood Fill"),
    METAL_FILL("Metal Fill"),
    GALAXY("Galaxy/Sparkle"),
    TRANSLUCENT("Translucent");

    companion object {
        fun fromString(value: String): FillType? =
            entries.find {
                it.name.equals(value, ignoreCase = true) ||
                    it.displayName.equals(value, ignoreCase = true)
            }

        val allDisplayNames: List<String> = entries.map { it.displayName }
    }
}
