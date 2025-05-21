package com.example.timeblock.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.timeblock.data.dao.EntryDao
import com.example.timeblock.data.dao.UserDao
import com.example.timeblock.data.entity.Entry
import com.example.timeblock.data.entity.User
import com.example.timeblock.util.DateTypeConverters

@Database(entities = [User::class, Entry::class], version = 2, exportSchema = false)
@TypeConverters(DateTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun entryDao(): EntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "timeblock_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
