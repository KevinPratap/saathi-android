package com.saathi.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.saathi.data.entity.AuditLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for irreversible audit logs.
 */
@Dao
interface AuditLogDao {
    @Insert
    suspend fun insertLog(log: AuditLogEntity): Long

    @Query("SELECT * FROM audit_logs ORDER BY timestamp_ms DESC LIMIT :limit")
    suspend fun getRecentLogs(limit: Int): List<AuditLogEntity>

    @Query("SELECT * FROM audit_logs ORDER BY timestamp_ms DESC")
    fun observeAllLogs(): Flow<List<AuditLogEntity>>

    @Query("DELETE FROM audit_logs WHERE timestamp_ms < :cutoffTimestampMs")
    suspend fun purgeLogsOlderThan(cutoffTimestampMs: Long): Int

    @Query("SELECT COUNT(*) FROM audit_logs")
    suspend fun countLogs(): Int
}
