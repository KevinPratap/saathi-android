package com.saathi.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.saathi.data.dao.AuditLogDao
import com.saathi.data.dao.PatternDao
import com.saathi.data.dao.UserPreferencesDao
import com.saathi.data.entity.AuditLogEntity
import com.saathi.data.entity.PatternEntity
import com.saathi.data.entity.UserPreferencesEntity

@Database(
    entities = [
        PatternEntity::class,
        AuditLogEntity::class,
        UserPreferencesEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun patternDao(): PatternDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun userPreferencesDao(): UserPreferencesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "saathi_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
