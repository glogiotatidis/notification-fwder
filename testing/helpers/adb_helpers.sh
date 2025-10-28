#!/bin/bash

# ADB helper functions for testing

PACKAGE_NAME="com.notificationforwarder"
MAIN_ACTIVITY="$PACKAGE_NAME.MainActivity"
SERVICE_NAME="$PACKAGE_NAME.service.NotificationForwarderService"

# Check if device is connected
check_device_connected() {
    adb devices | grep -q "device$"
}

# Check if app is installed
is_app_installed() {
    adb shell pm list packages | grep -q "^package:$PACKAGE_NAME$"
}

# Install APK
install_apk() {
    local apk_path=$1
    adb install -r "$apk_path"
}

# Uninstall app
uninstall_app() {
    adb uninstall "$PACKAGE_NAME" 2>/dev/null || true
}

# Launch app
launch_app() {
    adb shell am start -n "$MAIN_ACTIVITY"
}

# Force stop app
stop_app() {
    adb shell am force-stop "$PACKAGE_NAME"
}

# Clear app data
clear_app_data() {
    adb shell pm clear "$PACKAGE_NAME"
}

# Check if service is running
is_service_running() {
    adb shell dumpsys activity services | grep -q "$SERVICE_NAME"
}

# Start service
start_service() {
    adb shell am start-foreground-service -n "$SERVICE_NAME"
}

# Check if notification listener is enabled
is_notification_listener_enabled() {
    adb shell settings get secure enabled_notification_listeners | grep -q "$PACKAGE_NAME"
}

# Enable notification listener
enable_notification_listener() {
    local listeners=$(adb shell settings get secure enabled_notification_listeners | tr -d '\r')
    if [ -z "$listeners" ] || [ "$listeners" = "null" ]; then
        adb shell settings put secure enabled_notification_listeners "$PACKAGE_NAME/$SERVICE_NAME"
    else
        if ! echo "$listeners" | grep -q "$PACKAGE_NAME"; then
            adb shell settings put secure enabled_notification_listeners "$listeners:$PACKAGE_NAME/$SERVICE_NAME"
        fi
    fi
}

# Send test notification
send_test_notification() {
    local title=$1
    local text=$2
    local package=${3:-"com.test.app"}

    # Use adb shell to trigger a notification via test app or system notification
    # This is a simplified version - in reality you'd need a test app
    adb shell "cmd notification post -t '$title' '$package' 'test_tag' '$text'"
}

# Get app version
get_app_version() {
    adb shell dumpsys package "$PACKAGE_NAME" | grep versionName | head -1 | awk '{print $1}' | cut -d'=' -f2
}

# Get logcat for app
get_app_logs() {
    local lines=${1:-100}
    adb logcat -d | grep "$PACKAGE_NAME" | tail -n "$lines"
}

# Clear logcat
clear_logcat() {
    adb logcat -c
}

# Wait for condition with timeout
wait_for_condition() {
    local condition=$1
    local timeout=${2:-30}
    local count=0

    while [ $count -lt $timeout ]; do
        if eval "$condition"; then
            return 0
        fi
        sleep 1
        count=$((count + 1))
    done

    return 1
}

# Execute SQL query on app database
execute_sql() {
    local query=$1
    adb shell "run-as $PACKAGE_NAME sqlite3 databases/notification_forwarder_db \"$query\""
}

# Count rows in table
count_table_rows() {
    local table=$1
    execute_sql "SELECT COUNT(*) FROM $table;"
}

# Tap UI element by text
tap_by_text() {
    local text=$1
    adb shell "input tap \$(dumpsys window | grep -A 20 'mCurrentFocus' | grep '$text' | awk '{print \$4, \$6}')"
}

# Input text
input_text() {
    local text=$1
    adb shell input text "'$text'"
}

# Press back button
press_back() {
    adb shell input keyevent KEYCODE_BACK
}

# Take screenshot
take_screenshot() {
    local filename=$1
    adb exec-out screencap -p > "$filename"
}

# Get device info
get_device_info() {
    echo "Model: $(adb shell getprop ro.product.model | tr -d '\r')"
    echo "Android: $(adb shell getprop ro.build.version.release | tr -d '\r')"
    echo "API: $(adb shell getprop ro.build.version.sdk | tr -d '\r')"
}

# Check if permission is granted
is_permission_granted() {
    local permission=$1
    adb shell dumpsys package "$PACKAGE_NAME" | grep "$permission" | grep -q "granted=true"
}

# Grant permission
grant_permission() {
    local permission=$1
    adb shell pm grant "$PACKAGE_NAME" "$permission"
}

