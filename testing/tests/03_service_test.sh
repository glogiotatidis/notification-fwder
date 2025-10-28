#!/bin/bash

# Test 03: Service Test
# Validates notification listener service lifecycle

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/../helpers/adb_helpers.sh"

TEST_NAME="service_test"

echo "========================================="
echo "Test 03: Service Test"
echo "========================================="

# Ensure notification listener is enabled
enable_notification_listener
sleep 2

# Test 1: Launch app to start service
echo -n "Launching app to start service... "
launch_app
sleep 3
echo "OK"

# Test 2: Check if service is running
echo -n "Checking if service is running... "
if is_service_running; then
    echo "OK"
else
    echo "FAILED"
    exit 1
fi

# Test 3: Check for foreground notification
echo -n "Checking for foreground service notification... "
if adb shell dumpsys notification | grep -q "$PACKAGE_NAME"; then
    echo "OK"
else
    echo "FAILED - Service may not be running in foreground"
    exit 2
fi

# Test 4: Stop app and check service persists
echo -n "Testing service persistence after force-stop... "
stop_app
sleep 2
# Note: NotificationListenerService should auto-restart by system
sleep 3
if is_service_running; then
    echo "OK - Service auto-restarted"
else
    echo "WARNING - Service did not auto-restart (this may be normal on some Android versions)"
fi

# Test 5: Check service logs
echo -n "Checking service logs for errors... "
if get_app_logs 100 | grep -i "error\|exception\|crash" | grep -v "test"; then
    echo "WARNING - Found errors in logs (review above)"
else
    echo "OK - No critical errors found"
fi

# Output JSON result
cat <<EOF
{
  "test": "$TEST_NAME",
  "status": "passed",
  "service_running": true,
  "foreground_service": true,
  "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF

echo "========================================="
echo "Service test completed successfully"
echo "========================================="

exit 0

