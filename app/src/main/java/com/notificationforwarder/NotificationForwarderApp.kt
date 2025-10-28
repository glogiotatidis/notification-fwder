package com.notificationforwarder

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NotificationForwarderApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}

