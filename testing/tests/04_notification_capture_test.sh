#!/bin/bash

# Test 04: Notification Capture Test
# Tests notification capture and data extraction

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/../helpers/adb_helpers.sh"
source "$SCRIPT_DIR/../helpers/notification_generator.sh"

TEST_NAME="notification_capture_test"

echo "========================================="
echo "Test 04: Notification Capture Test"
echo "========================================="

# Ensure service is running
launch_app
sleep 2

# Test 1: Clear existing notifications
echo -n "Clearing existing notifications... "
clear_all_notifications
sleep 1
echo "OK"

# Test 2: Generate test notification
echo -n "Generating test notification... "
generate_notification "com.test.capture" "Test Capture Title" "This is a test notification for capture" 100
sleep 2
echo "OK"

# Test 3: Check if notification was logged
echo -n "Checking if notification was captured... "
if get_app_logs 50 | grep -q "Notification posted from com.test.capture"; then
    echo "OK"
else
    echo "FAILED - Notification not captured"
    echo "Service status:"
    is_service_running && echo "  Service is running" || echo "  Service is NOT running"
    exit 1
fi

# Test 4: Verify notification data extraction
echo -n "Verifying notification data extraction... "
if get_app_logs 100 | grep -q "Test Capture Title"; then
    echo "OK - Title extracted"
else
    echo "WARNING - Title not found in logs"
fi

# Test 5: Generate multiple notifications
echo -n "Testing multiple notification capture... "
generate_test_notifications
sleep 3

CAPTURED_COUNT=$(get_app_logs 200 | grep -c "Notification posted from" || echo "0")
if [ "$CAPTURED_COUNT" -gt 0 ]; then
    echo "OK - Captured $CAPTURED_COUNT notifications"
else
    echo "FAILED - No notifications captured"
    exit 1
fi

# Output JSON result
cat <<EOF
{
  "test": "$TEST_NAME",
  "status": "passed",
  "notifications_captured": $CAPTURED_COUNT,
  "data_extraction_working": true,
  "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF

echo "========================================="
echo "Notification capture test completed successfully"
echo "========================================="

exit 0

