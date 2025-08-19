package com.example.timeblock.util

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GarminStepsProviderTest {
    @Test
    fun forerunner255SampleSteps() = runBlocking {
        val sampleSteps = 7890L // sample steps from a Garmin Forerunner 255
        val provider = object : StepCountProvider {
            override suspend fun getTodaySteps(): Long = sampleSteps
        }
        assertEquals(sampleSteps, provider.getTodaySteps())
    }
}
