package com.saathi.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.saathi.model.ScamCategory

/**
 * Persisted scam pattern signature loaded into Trie/Regex matching engine.
 */
@Entity(tableName = "patterns")
data class PatternEntity(
    @PrimaryKey
    @ColumnInfo(name = "pattern_id")
    val patternId: String,

    @ColumnInfo(name = "category")
    val category: ScamCategory,

    @ColumnInfo(name = "base_weight")
    val baseWeight: Int,

    @ColumnInfo(name = "regex_rule")
    val regexRule: String?,

    @ColumnInfo(name = "keywords_csv")
    val keywordsCsv: String,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "updated_at_ms")
    val updatedAtMs: Long = System.currentTimeMillis()
)
