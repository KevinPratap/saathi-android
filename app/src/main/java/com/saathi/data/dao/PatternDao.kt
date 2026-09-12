package com.saathi.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.saathi.data.entity.PatternEntity

/**
 * Data Access Object for scam patterns and rules.
 */
@Dao
interface PatternDao {
    @Query("SELECT * FROM patterns WHERE is_active = 1")
    suspend fun getActivePatterns(): List<PatternEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatterns(patterns: List<PatternEntity>)

    @Query("DELETE FROM patterns WHERE pattern_id = :patternId")
    suspend fun deletePattern(patternId: String)

    @Query("SELECT COUNT(*) FROM patterns")
    suspend fun countPatterns(): Int
}
