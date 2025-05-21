package com.example.timeblock.data

import com.example.timeblock.data.dao.EntryDao
import com.example.timeblock.data.dao.UserDao
import com.example.timeblock.data.entity.Entry
import com.example.timeblock.data.entity.User
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class Repository(private val userDao: UserDao, private val entryDao: EntryDao) {

    // User operations
    suspend fun insertUser(displayName: String, weight: String): User {
        val now = Instant.now()
        val user = User(
            userUuid = UUID.randomUUID().toString(),
            displayName = displayName,
            weight = weight,
            timeCreated = now,
            timeModified = now
        )
        userDao.insert(user)
        return user
    }

    suspend fun getUser(): User? {
        val users = userDao.getAllUsers()
        return users.firstOrNull()
    }

    suspend fun updateUser(user: User, newDisplayName: String, newWeight: String): User {
        val updated = user.copy(
            displayName = newDisplayName,
            weight = newWeight,
            timeModified = Instant.now()
        )
        userDao.updateUser(updated.displayName, updated.weight, updated.timeModified, updated.userUuid)
        return updated
    }

    // Entry operations
    suspend fun getOrCreateTodayEntry(): Entry {
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusMillis(1)

        val todayEntries = entryDao.getEntriesBetween(startOfDay, endOfDay)
        return if (todayEntries.isNotEmpty()) {
            todayEntries.first()
        } else {
            val now = Instant.now()
            val entry = Entry(
                proteinGrams = 0,
                vegetableServings = 0,
                steps = 0,
                timeCreated = now,
                timeModified = now
            )
            entryDao.insert(entry)
            entry
        }
    }

    suspend fun updateProtein(value: Int, isAddition: Boolean): Entry {
        val entry = getOrCreateTodayEntry()
        val updatedEntry = entry.copy(
            proteinGrams = if (isAddition) entry.proteinGrams + value else value,
            timeModified = Instant.now()
        )
        entryDao.insert(updatedEntry)
        return updatedEntry
    }

    suspend fun updateVegetables(value: Int, isAddition: Boolean): Entry {
        val entry = getOrCreateTodayEntry()
        val updatedEntry = entry.copy(
            vegetableServings = if (isAddition) entry.vegetableServings + value else value,
            timeModified = Instant.now()
        )
        entryDao.insert(updatedEntry)
        return updatedEntry
    }

    suspend fun updateSteps(value: Int, isAddition: Boolean): Entry {
        val entry = getOrCreateTodayEntry()
        val updatedEntry = entry.copy(
            steps = if (isAddition) entry.steps + value else value,
            timeModified = Instant.now()
        )
        entryDao.insert(updatedEntry)
        return updatedEntry
    }

    fun getTodayEntryFlow(): Flow<Entry> = flow {
        emit(getOrCreateTodayEntry())
    }

    suspend fun getAllEntries(): List<Entry> {
        return entryDao.getAllEntries()
    }

    fun getAllEntriesFlow(): Flow<List<Entry>> = flow {
        emit(getAllEntries())
    }


}
