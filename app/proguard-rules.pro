# Add project specific ProGuard rules here.
# Keep Retrofit and Gson classes
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }

# Keep Room entities
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
-keep class javax.inject.** { *; }

# Keep notification data classes
-keep class com.notificationforwarder.data.** { *; }

# Keep service classes
-keep class * extends android.service.notification.NotificationListenerService { *; }
-keep class * extends android.content.BroadcastReceiver { *; }

