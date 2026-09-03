package com.saathi.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.saathi.model.RiskLevel
import com.saathi.model.ScamCategory

/**
 * Anonymized, irreversible tamper-evident security audit record.
 */
@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "timestamp_ms")
    val timestampMs: Long,

    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "category")
    val category: ScamCategory,

    @ColumnInfo(name = "risk_level")
    val riskLevel: RiskLevel,

    @ColumnInfo(name = "confidence_score")
    val confidenceScore: Int,

    @ColumnInfo(name = "anonymized_audit_hash")
    val anonymizedAuditHash: String,

    @ColumnInfo(name = "action_taken")
    val actionTaken: String
)
