package com.notificationforwarder.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add useRegex column to trigger_rules table with default value false
            database.execSQL(
                "ALTER TABLE trigger_rules ADD COLUMN useRegex INTEGER NOT NULL DEFAULT 0"
            )
        }
    }
}

