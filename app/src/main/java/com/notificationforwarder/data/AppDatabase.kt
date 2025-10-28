package com.notificationforwarder.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.notificationforwarder.data.converter.MapTypeConverter
import com.notificationforwarder.data.dao.NotificationLogDao
import com.notificationforwarder.data.dao.TriggerRuleDao
import com.notificationforwarder.data.dao.WebhookConfigDao
import com.notificationforwarder.data.entity.NotificationLog
import com.notificationforwarder.data.entity.TriggerRule
import com.notificationforwarder.data.entity.WebhookConfig

@Database(
    entities = [
        WebhookConfig::class,
        TriggerRule::class,
        NotificationLog::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(MapTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun webhookConfigDao(): WebhookConfigDao
    abstract fun triggerRuleDao(): TriggerRuleDao
    abstract fun notificationLogDao(): NotificationLogDao
}

