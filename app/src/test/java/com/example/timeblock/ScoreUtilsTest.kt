package com.example.timeblock

import com.example.timeblock.data.entity.Entry
import com.example.timeblock.util.ScoreUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class ScoreUtilsTest {
    private fun entry(p: Int, v: Int, s: Int) = Entry(
        proteinGrams = p,
        vegetableServings = v,
        steps = s,
        timeCreated = Instant.EPOCH,
        timeModified = Instant.EPOCH
    )

    @Test
    fun perfectScore() {
        val e = entry(100, 5, 8500)
        val score = ScoreUtils.calculateScore(e, "100 kg")
        assertEquals(100, score)
    }

    @Test
    fun halfScore() {
        val e = entry(50, 2, 4250)
        val score = ScoreUtils.calculateScore(e, "100 kg")
        assertEquals(50, score)
    }

    @Test
    fun cappedScore() {
        val e = entry(200, 10, 20000)
        val score = ScoreUtils.calculateScore(e, "100 kg")
        assertEquals(100, score)
    }
}
