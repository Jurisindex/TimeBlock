package com.example.timeblock.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "Entry")
data class Entry(
    @PrimaryKey(autoGenerate = true) val entryId: Int = 0,
    @ColumnInfo(name = "protein_grams") val proteinGrams: Int,
    @ColumnInfo(name = "vegetable_servings") val vegetableServings: Int,
    @ColumnInfo(name = "steps") val steps: Int,
    @ColumnInfo(name = "time_created") val timeCreated: Instant,
    @ColumnInfo(name = "time_modified") val timeModified: Instant
)
