package com.example.timeblock.data

import com.example.timeblock.data.dao.EntryDao
import com.example.timeblock.data.dao.UserDao
import com.example.timeblock.data.entity.Entry
import com.example.timeblock.data.entity.User
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FakeEntryDao : EntryDao {
    private val entries = mutableListOf<Entry>()
    private var nextId = 1

    override suspend fun insert(entry: Entry) {
        val idx = entries.indexOfFirst { it.entryId == entry.entryId && entry.entryId != 0 }
        if (entry.entryId == 0) {
            entries.add(entry.copy(entryId = nextId++))
        } else if (idx >= 0) {
            entries[idx] = entry
        } else {
            entries.add(entry)
        }
    }

    override suspend fun getAllEntries(): List<Entry> = entries.toList()

    override suspend fun getEntriesBetween(start: Instant, end: Instant): List<Entry> {
        return entries.filter { it.timeCreated >= start && it.timeCreated <= end }
    }

    override suspend fun getEntryById(id: Int): Entry? = entries.find { it.entryId == id }

    override suspend fun deleteById(id: Int) { entries.removeIf { it.entryId == id } }
}

class FakeUserDao : UserDao {
    var user: User? = null

    override suspend fun insert(user: User) {
        this.user = user
    }

    override suspend fun getAllUsers(): List<User> = user?.let { listOf(it) } ?: emptyList()

    override suspend fun updateUser(
        displayName: String,
        weight: String,
        timeModified: Instant,
        garminDeviceId: String?,
        lastSynced: Instant?,
        userUuid: String
    ) {
        user = user?.copy(
            displayName = displayName,
            weight = weight,
            timeModified = timeModified,
            garminDeviceId = garminDeviceId,
            lastSynced = lastSynced
        )
    }
}

class RepositoryImportGarminStepsTest {
    private lateinit var repository: Repository
    private lateinit var entryDao: FakeEntryDao
    private lateinit var userDao: FakeUserDao

    @Before
    fun setup() {
        entryDao = FakeEntryDao()
        userDao = FakeUserDao()
        repository = Repository(userDao, entryDao)
    }

    @Test
    fun createsNewEntryWhenMissing() = runBlocking {
        val date = LocalDate.now().minusDays(1)
        repository.importGarminSteps(mapOf(date to 100))
        val start = date.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val end = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusMillis(1)
        val entries = entryDao.getEntriesBetween(start, end)
        assertEquals(1, entries.size)
        assertEquals(100, entries.first().steps)
    }

    @Test
    fun updatesExistingEntry() = runBlocking {
        val date = LocalDate.now().minusDays(1)
        repository.insertEntryOn(date)
        repository.importGarminSteps(mapOf(date to 200))
        val start = date.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val end = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusMillis(1)
        val entry = entryDao.getEntriesBetween(start, end).first()
        assertEquals(200, entry.steps)
    }

    @Test
    fun lastSyncedUpdated() = runBlocking {
        val now = Instant.now()
        val user = User(
            userUuid = "uid",
            displayName = "Test",
            weight = "0",
            timeCreated = now,
            timeModified = now,
            garminDeviceId = null,
            lastSynced = null
        )
        userDao.insert(user)

        repository.importGarminSteps(emptyMap())
        val syncTime = now.plusSeconds(5)
        repository.updateLastSynced(syncTime)

        val stored = userDao.getAllUsers().first()
        assertEquals(syncTime, stored.lastSynced)
    }
}
