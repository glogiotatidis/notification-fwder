package com.notificationforwarder.di

import android.content.Context
import androidx.room.Room
import com.notificationforwarder.data.AppDatabase
import com.notificationforwarder.data.dao.NotificationLogDao
import com.notificationforwarder.data.dao.TriggerRuleDao
import com.notificationforwarder.data.dao.WebhookConfigDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "notification_forwarder_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideWebhookConfigDao(database: AppDatabase): WebhookConfigDao {
        return database.webhookConfigDao()
    }

    @Provides
    fun provideTriggerRuleDao(database: AppDatabase): TriggerRuleDao {
        return database.triggerRuleDao()
    }

    @Provides
    fun provideNotificationLogDao(database: AppDatabase): NotificationLogDao {
        return database.notificationLogDao()
    }
}

