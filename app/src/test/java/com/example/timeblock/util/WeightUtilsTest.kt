package com.example.timeblock.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WeightUtilsTest {
    @Test
    fun parseCommonFormats() {
        assertEquals(234, parseWeight("234kg")!!.value.toInt())
        assertEquals(234, parseWeight("234 kg")!!.value.toInt())
        assertEquals(234, parseWeight("234 kgs")!!.value.toInt())
        assertEquals(234, parseWeight("234 kilograms")!!.value.toInt())
        assertEquals(100, parseWeight("100 lb")!!.value.toInt())
        assertEquals(100, parseWeight("100 lbs")!!.value.toInt())
        assertEquals(100, parseWeight("100 pounds")!!.value.toInt())
    }

    @Test
    fun parseNumericOnly() {
        assertEquals(50, parseWeight("50")!!.value.toInt())
    }

    @Test
    fun invalidInput() {
        assertNull(parseWeight("abc"))
    }
}
