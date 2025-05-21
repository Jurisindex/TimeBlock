package com.example.timeblock

import com.example.timeblock.util.Weight
import com.example.timeblock.util.Weight.Unit
import com.example.timeblock.util.parseWeight
import com.example.timeblock.util.proteinGoalForWeightString
import org.junit.Assert.*
import org.junit.Test

class WeightUtilsTest {
    @Test
    fun parseKilograms() {
        val weight = parseWeight("23 kg")
        assertNotNull(weight)
        assertEquals(Weight(23.0, Unit.KG), weight)
    }

    @Test
    fun parsePounds() {
        val weight = parseWeight("23 lbs")
        assertNotNull(weight)
        assertEquals(Weight(23.0, Unit.LBS), weight)
    }

    @Test
    fun proteinGoalKg() {
        val goal = proteinGoalForWeightString("23 kg")
        assertEquals(46, goal)
    }

    @Test
    fun proteinGoalLbs() {
        val goal = proteinGoalForWeightString("23 lbs")
        assertEquals(21, goal)
    }
}
