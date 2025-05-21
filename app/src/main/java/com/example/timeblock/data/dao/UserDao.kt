package com.example.timeblock.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.timeblock.data.entity.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("SELECT * FROM User")
    suspend fun getAllUsers(): List<User>

    @Query("UPDATE User SET display_name = :displayName, weight = :weight, time_modified = :timeModified WHERE user_uuid = :userUuid")
    suspend fun updateUser(displayName: String, weight: String, timeModified: java.time.Instant, userUuid: String)

    // Add more methods as needed (e.g., update, delete, query by ID)
}
