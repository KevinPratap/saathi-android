package com.saathi.data

import androidx.room.TypeConverter
import com.saathi.model.RiskLevel
import com.saathi.model.ScamCategory

class Converters {
    @TypeConverter
    fun fromScamCategory(value: ScamCategory): String = value.name

    @TypeConverter
    fun toScamCategory(value: String): ScamCategory = try {
        ScamCategory.valueOf(value)
    } catch (_: Exception) {
        ScamCategory.GENERIC_SUSPICIOUS
    }

    @TypeConverter
    fun fromRiskLevel(value: RiskLevel): String = value.name

    @TypeConverter
    fun toRiskLevel(value: String): RiskLevel = try {
        RiskLevel.valueOf(value)
    } catch (_: Exception) {
        RiskLevel.SAFE
    }
}
