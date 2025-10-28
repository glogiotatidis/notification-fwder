#!/bin/bash

# Test 02: Permissions Test
# Checks all required permissions are declared and can be granted

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/../helpers/adb_helpers.sh"

TEST_NAME="permissions_test"

echo "========================================="
echo "Test 02: Permissions Test"
echo "========================================="

# Test 1: Check INTERNET permission
echo -n "Checking INTERNET permission... "
if adb shell dumpsys package "$PACKAGE_NAME" | grep -q "android.permission.INTERNET"; then
    echo "OK"
else
    echo "FAILED"
    exit 1
fi

# Test 2: Check POST_NOTIFICATIONS permission
echo -n "Checking POST_NOTIFICATIONS permission... "
if adb shell dumpsys package "$PACKAGE_NAME" | grep -q "android.permission.POST_NOTIFICATIONS"; then
    echo "OK"
else
    echo "FAILED"
    exit 1
fi

# Test 3: Check RECEIVE_BOOT_COMPLETED permission
echo -n "Checking RECEIVE_BOOT_COMPLETED permission... "
if adb shell dumpsys package "$PACKAGE_NAME" | grep -q "android.permission.RECEIVE_BOOT_COMPLETED"; then
    echo "OK"
else
    echo "FAILED"
    exit 1
fi

# Test 4: Check FOREGROUND_SERVICE permission
echo -n "Checking FOREGROUND_SERVICE permission... "
if adb shell dumpsys package "$PACKAGE_NAME" | grep -q "android.permission.FOREGROUND_SERVICE"; then
    echo "OK"
else
    echo "FAILED"
    exit 1
fi

# Test 5: Check Notification Listener Service declaration
echo -n "Checking NotificationListenerService declaration... "
if adb shell dumpsys package "$PACKAGE_NAME" | grep -q "NotificationListenerService"; then
    echo "OK"
else
    echo "FAILED"
    exit 1
fi

# Test 6: Enable notification listener
echo -n "Enabling notification listener... "
enable_notification_listener
sleep 2
if is_notification_listener_enabled; then
    echo "OK"
else
    echo "FAILED - Manual action may be required"
    echo "Please enable notification access manually:"
    echo "  Settings → Apps → Notification Forwarder → Notifications → Allow"
    exit 2
fi

# Output JSON result
cat <<EOF
{
  "test": "$TEST_NAME",
  "status": "passed",
  "permissions_checked": [
    "INTERNET",
    "POST_NOTIFICATIONS",
    "RECEIVE_BOOT_COMPLETED",
    "FOREGROUND_SERVICE",
    "BIND_NOTIFICATION_LISTENER_SERVICE"
  ],
  "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF

echo "========================================="
echo "Permissions test completed successfully"
echo "========================================="

exit 0

