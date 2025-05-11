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

    // Add more methods as needed (e.g., update, delete, query by ID)
}
