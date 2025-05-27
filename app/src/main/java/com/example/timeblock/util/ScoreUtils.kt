package com.example.timeblock.util

import com.example.timeblock.data.entity.Entry
import kotlin.math.min
import kotlin.math.roundToInt

/** Utility functions for computing a daily score based on tracking data. */
object ScoreUtils {
    /**
     * Calculate a score from the given metrics. Values are capped at 100.
     */
    fun calculateScore(
        proteinGrams: Int,
        proteinGoal: Int,
        veggieServings: Int,
        veggieGoal: Int = 5,
        steps: Int,
        stepsGoal: Int = 8500
    ): Int {
        if (proteinGoal <= 0 || veggieGoal <= 0 || stepsGoal <= 0) return 0
        val p = min(proteinGrams.toDouble() / proteinGoal, 1.0)
        val v = min(veggieServings.toDouble() / veggieGoal, 1.0)
        val s = min(steps.toDouble() / stepsGoal, 1.0)
        return ((p + v + s) / 3.0 * 100).roundToInt()
    }

    /** Convenience wrapper that calculates the score for an [Entry] and weight string. */
    fun calculateScore(entry: Entry, weight: String): Int {
        val proteinGoal = proteinGoalForWeightString(weight)
        return calculateScore(entry.proteinGrams, proteinGoal, entry.vegetableServings, 5, entry.steps, 8500)
    }

    /**
     * Format metrics into a single text block that includes the calculated score.
     */
    fun formatMetrics(entry: Entry, weight: String): String {
        val score = calculateScore(entry, weight)
        return "Score: $score | Protein: ${entry.proteinGrams}g | Veggies: ${entry.vegetableServings} | Steps: ${entry.steps}"
    }
}
