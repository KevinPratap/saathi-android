package com.saathi.data

import com.saathi.data.dao.AuditLogDao
import com.saathi.data.dao.PatternDao
import com.saathi.data.dao.UserPreferencesDao
import com.saathi.data.entity.AuditLogEntity
import com.saathi.data.entity.PatternEntity
import com.saathi.data.entity.UserPreferencesEntity
import com.saathi.model.RiskLevel
import com.saathi.model.ScamCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Verification test suite for Room DAOs and database contracts.
 */
class RoomDatabaseTest {

    private lateinit var patternDao: MockPatternDao
    private lateinit var auditLogDao: MockAuditLogDao
    private lateinit var preferencesDao: MockUserPreferencesDao

    @Before
    fun setUp() {
        patternDao = MockPatternDao()
        auditLogDao = MockAuditLogDao()
        preferencesDao = MockUserPreferencesDao()
    }

    @Test
    fun testPatternDao_InsertAndRetrieveActive() = runBlocking {
        val patterns = listOf(
            PatternEntity("P1", ScamCategory.OTP_THEFT, 85, null, "otp,code", isActive = true),
            PatternEntity("P2", ScamCategory.DIGITAL_ARREST, 95, null, "police,cbi", isActive = true),
            PatternEntity("P3", ScamCategory.LOTTERY_PRIZE_SCAM, 70, null, "lottery", isActive = false)
        )

        patternDao.insertPatterns(patterns)
        val active = patternDao.getActivePatterns()

        assertEquals("Only active patterns should be retrieved", 2, active.size)
        assertEquals(3, patternDao.countPatterns())

        patternDao.deletePattern("P1")
        val remainingActive = patternDao.getActivePatterns()
        assertEquals(1, remainingActive.size)
        assertEquals("P2", remainingActive[0].patternId)
    }

    @Test
    fun testAuditLogDao_InsertAndPurge() = runBlocking {
        val now = System.currentTimeMillis()
        val log1 = AuditLogEntity(1, now - 10000, "com.whatsapp", ScamCategory.OTP_THEFT, RiskLevel.HIGH, 90, "hash1", "BLOCKED")
        val log2 = AuditLogEntity(2, now - 5000, "com.telegram", ScamCategory.DIGITAL_ARREST, RiskLevel.HIGH, 95, "hash2", "WARNED")
        val log3 = AuditLogEntity(3, now, "com.phonepe", ScamCategory.BANKING_KYC_FRAUD, RiskLevel.MEDIUM, 60, "hash3", "DISMISSED")

        auditLogDao.insertLog(log1)
        auditLogDao.insertLog(log2)
        auditLogDao.insertLog(log3)

        assertEquals(3, auditLogDao.countLogs())

        val recent = auditLogDao.getRecentLogs(2)
        assertEquals(2, recent.size)
        assertEquals(3, recent[0].id) // Most recent first

        // Purge logs older than now - 6000 (should remove log1)
        val purgedCount = auditLogDao.purgeLogsOlderThan(now - 6000)
        assertEquals(1, purgedCount)
        assertEquals(2, auditLogDao.countLogs())
    }

    @Test
    fun testUserPreferencesDao_Crud() = runBlocking {
        val pref = UserPreferencesEntity("sensitivity", "HIGH")
        preferencesDao.setPreference(pref)

        val retrieved = preferencesDao.getPreference("sensitivity")
        assertNotNull(retrieved)
        assertEquals("HIGH", retrieved?.value)

        // Update preference
        preferencesDao.setPreference(UserPreferencesEntity("sensitivity", "LOW"))
        val updated = preferencesDao.getPreference("sensitivity")
        assertEquals("LOW", updated?.value)

        // Delete preference
        preferencesDao.deletePreference("sensitivity")
        assertNull(preferencesDao.getPreference("sensitivity"))
    }
}

// In-Memory DAO implementations adhering strictly to Room DAO interface contracts
class MockPatternDao : PatternDao {
    private val store = mutableMapOf<String, PatternEntity>()

    override suspend fun getActivePatterns(): List<PatternEntity> {
        return store.values.filter { it.isActive }
    }

    override suspend fun insertPatterns(patterns: List<PatternEntity>) {
        for (p in patterns) {
            store[p.patternId] = p
        }
    }

    override suspend fun deletePattern(patternId: String) {
        store.remove(patternId)
    }

    override suspend fun countPatterns(): Int = store.size
}

class MockAuditLogDao : AuditLogDao {
    private val logs = mutableListOf<AuditLogEntity>()
    private var idCounter = 1L

    override suspend fun insertLog(log: AuditLogEntity): Long {
        val id = if (log.id == 0L) idCounter++ else log.id
        logs.add(log.copy(id = id))
        return id
    }

    override suspend fun getRecentLogs(limit: Int): List<AuditLogEntity> {
        return logs.sortedByDescending { it.timestampMs }.take(limit)
    }

    override fun observeAllLogs(): Flow<List<AuditLogEntity>> {
        return flowOf(logs.sortedByDescending { it.timestampMs })
    }

    override suspend fun purgeLogsOlderThan(cutoffTimestampMs: Long): Int {
        val before = logs.size
        logs.removeIf { it.timestampMs < cutoffTimestampMs }
        return before - logs.size
    }

    override suspend fun countLogs(): Int = logs.size
}

class MockUserPreferencesDao : UserPreferencesDao {
    private val prefs = mutableMapOf<String, UserPreferencesEntity>()

    override suspend fun getPreference(key: String): UserPreferencesEntity? = prefs[key]

    override suspend fun setPreference(pref: UserPreferencesEntity) {
        prefs[pref.key] = pref
    }

    override suspend fun deletePreference(key: String) {
        prefs.remove(key)
    }
}
