package com.notificationforwarder.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == "android.intent.action.QUICKBOOT_POWERON") {

            Log.d(TAG, "Boot completed, starting notification service")

            // The NotificationListenerService will be automatically started by the system
            // if it has the proper permissions. We just log the boot event here.

            // Optional: You could start an activity to remind user to enable the service
            // if it's not already enabled
        }
    }
}

