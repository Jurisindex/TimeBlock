package com.example.timeblock

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun weightParsing_andGoalCalculation() {
        val wKg = com.example.timeblock.util.parseWeight("23 kg")
        val wLb = com.example.timeblock.util.parseWeight("23 lbs")

        assertNotNull(wKg)
        assertNotNull(wLb)

        wKg!!
        wLb!!

        assertEquals(23.0, wKg.value, 0.001)
        assertEquals(com.example.timeblock.util.Weight.Unit.KG, wKg.unit)

        assertEquals(23.0, wLb.value, 0.001)
        assertEquals(com.example.timeblock.util.Weight.Unit.LBS, wLb.unit)

        val goalKg = com.example.timeblock.util.proteinGoalForWeightString("23 kg")
        assertEquals(46, goalKg)
    }
}