package com.example.timeblock.util

import kotlin.math.roundToInt

/** Represents a weight with value and unit. */
data class Weight(val value: Double, val unit: Unit) {
    enum class Unit { KG, LBS }
}

/** Parse a string like "70 kg" or "150 lbs" into a [Weight] object. */
fun parseWeight(weightString: String): Weight? {
    val trimmed = weightString.trim()
    val regex = Regex("""([0-9]+(?:\\.[0-9]+)?)\\s*(kg|lbs)""", RegexOption.IGNORE_CASE)
    val match = regex.find(trimmed)
    if (match != null) {
        val value = match.groupValues[1].toDoubleOrNull() ?: return null
        val unit = if (match.groupValues[2].equals("kg", ignoreCase = true)) Weight.Unit.KG else Weight.Unit.LBS
        return Weight(value, unit)
    }

    // Fallback: if the string only contains digits, assume kilograms
    val numeric = trimmed.toDoubleOrNull()
    if (numeric != null) {
        return Weight(numeric, Weight.Unit.KG)
    }

    return null
}

/** Calculate the recommended protein goal in grams for the given weight. */
fun proteinGoalForWeight(weight: Weight): Int {
    val grams = when (weight.unit) {
        Weight.Unit.KG -> weight.value * 1.98
        Weight.Unit.LBS -> weight.value * 0.9
    }
    return grams.roundToInt()
}

/** Convenience function to compute protein goal directly from a weight string. */
fun proteinGoalForWeightString(weightString: String): Int {
    val weight = parseWeight(weightString) ?: return 0
    return proteinGoalForWeight(weight)
}
