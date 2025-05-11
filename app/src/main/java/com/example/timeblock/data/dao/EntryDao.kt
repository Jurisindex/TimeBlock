package com.example.timeblock.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.timeblock.data.entity.Entry
import java.time.Instant

@Dao
interface EntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: Entry)

    @Query("SELECT * FROM Entry")
    suspend fun getAllEntries(): List<Entry>

    @Query("SELECT * FROM Entry WHERE time_created BETWEEN :start AND :end")
    suspend fun getEntriesBetween(start: Instant, end: Instant): List<Entry>

    @Query("SELECT * FROM Entry WHERE entryId = :id")
    suspend fun getEntryById(id: Int): Entry?
}