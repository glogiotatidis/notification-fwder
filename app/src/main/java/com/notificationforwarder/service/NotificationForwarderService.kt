package com.notificationforwarder.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Base64
import android.util.Log
import androidx.core.app.NotificationCompat
import com.notificationforwarder.R
import com.notificationforwarder.data.model.NotificationData
import com.notificationforwarder.webhook.WebhookExecutor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class NotificationForwarderService : NotificationListenerService() {

    @Inject
    lateinit var webhookExecutor: WebhookExecutor

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "NotificationForwarder"
        private const val CHANNEL_ID = "notification_forwarder_channel"
        private const val NOTIFICATION_ID = 1
        
        private val _activeNotifications = mutableListOf<NotificationData>()
        
        @Synchronized
        fun getActiveNotifications(): List<NotificationData> {
            return _activeNotifications.toList()
        }
        
        @Synchronized
        fun updateActiveNotifications(notifications: List<NotificationData>) {
            _activeNotifications.clear()
            _activeNotifications.addAll(notifications)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createForegroundNotification())
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Listener connected")
        updateActiveNotificationsList()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let {
            Log.d(TAG, "Notification posted from ${it.packageName}")

            // Skip our own notifications
            if (it.packageName == packageName) {
                return
            }

            val notificationData = extractNotificationData(it)
            updateActiveNotificationsList()

            // Process webhook triggers
            serviceScope.launch {
                webhookExecutor.processNotification(notificationData)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        Log.d(TAG, "Notification removed from ${sbn?.packageName}")
        updateActiveNotificationsList()
    }

    private fun extractNotificationData(sbn: StatusBarNotification): NotificationData {
        val notification = sbn.notification
        val extras = notification.extras

        val appName = try {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(sbn.packageName, 0)
            ).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            sbn.packageName
        }

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        val priority = notification.priority

        // Extract and convert icon to base64
        val iconBase64 = extractIconAsBase64(sbn, notification)

        return NotificationData(
            packageName = sbn.packageName,
            appName = appName,
            title = title,
            text = text,
            subText = subText,
            bigText = bigText,
            priority = priority,
            timestamp = sbn.postTime,
            iconBase64 = iconBase64,
            key = sbn.key
        )
    }

    private fun extractIconAsBase64(sbn: StatusBarNotification, notification: Notification): String? {
        return try {
            // Try to get the small icon from notification
            val icon = notification.smallIcon
            val bitmap = if (icon != null) {
                // Convert Icon to Bitmap
                val drawable = icon.loadDrawable(this)
                drawable?.let { drawableToBitmap(it) }
            } else {
                // Fallback to app icon
                val appIcon = packageManager.getApplicationIcon(sbn.packageName)
                drawableToBitmap(appIcon)
            }

            // Convert bitmap to base64
            bitmap?.let { bitmapToBase64(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting icon: ${e.message}", e)
            null
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }

        val bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun updateActiveNotificationsList() {
        try {
            val currentNotifications = this.activeNotifications ?: emptyArray()
            val notificationDataList = currentNotifications.mapNotNull { sbn ->
                if (sbn.packageName != packageName) {
                    extractNotificationData(sbn)
                } else null
            }
            
            updateActiveNotifications(notificationDataList)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating active notifications: ${e.message}", e)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_service_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_service_channel_desc)
            setShowBadge(false)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.service_running))
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed")
    }
}

