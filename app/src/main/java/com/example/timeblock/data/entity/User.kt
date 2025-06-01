package com.example.timeblock.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "User")
data class User(
    @PrimaryKey(autoGenerate = true) val primaryKey: Int = 0,
    @ColumnInfo(name = "user_uuid") val userUuid: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "weight") val weight: String = "0",
    @ColumnInfo(name = "time_created") val timeCreated: Instant,
    @ColumnInfo(name = "time_modified") val timeModified: Instant,
    @ColumnInfo(name = "garmin_device_id") val garminDeviceId: String? = null,
    @ColumnInfo(name = "last_synced") val lastSynced: Instant? = null
)